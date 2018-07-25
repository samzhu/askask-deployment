package com.askask.deployment.service;

import com.askask.deployment.config.ServicesConfig;
import com.askask.deployment.dto.delivery.AppDeploymentResultCallback;
import com.askask.deployment.dto.delivery.AppDeploymentRqDto;
import com.askask.deployment.dto.deploy.conf.*;
import com.askask.deployment.dto.deploy.result.BackupResult;
import com.askask.deployment.dto.deploy.result.DeployResult;
import com.askask.deployment.dto.deploy.result.ServiceInfoResult;
import com.askask.deployment.dto.shell.ShellExecResult;
import com.askask.deployment.exception.NotFoundException;
import com.askask.deployment.exception.UnKnowException;
import com.askask.deployment.utils.DockerUtil;
import com.askask.deployment.utils.ShellUtil;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DeployService {
    @Autowired
    private ServicesConfig servicesConfig;

    @Async
    public void appDeploy(AppDeploymentRqDto appDeploymentRqDto) {
        String serviceId = appDeploymentRqDto.getServiceId();
        String tag = appDeploymentRqDto.getTag();
        AppDeploymentResultCallback appDeploymentResultCallback = new AppDeploymentResultCallback();
        appDeploymentResultCallback.setSequenceId(appDeploymentRqDto.getSequenceId());
        appDeploymentResultCallback.setServiceId(serviceId);
        appDeploymentResultCallback.setTag(tag);
        appDeploymentResultCallback.setNotifyId(appDeploymentRqDto.getNotifyId());
        appDeploymentResultCallback.setResultCallbackUrl(appDeploymentRqDto.getResultCallbackUrl());
        //
        Map<String, ServiceDeploy> serviceDeployMap = servicesConfig.getServices().stream().collect(
                Collectors.toMap(ServiceDeploy::getServiceId, Function.identity()));
        ServiceDeploy serviceDeploy = serviceDeployMap.get(serviceId);
        if (serviceDeploy == null) {
            throw new NotFoundException(serviceId + " Not Found");
        }
        BackupResult backupResult = this.backupInfo(serviceDeploy);
        List<DeployResult> deployResultList = new ArrayList<>();
        try {
            for (int i = 0; i < serviceDeploy.getDeployment().getReplicas(); i++) {
                DeployResult deployResult = this.deploy(serviceDeploy, tag);
                Boolean verifyResult = this.verify(deployResult, serviceDeploy.getDeployment().getVerify());
                if (verifyResult.booleanValue() == true) {
                    deployResultList.add(deployResult);
                    this.postDeploy(serviceDeploy, backupResult.getServiceInfoResultList(), deployResultList);
                } else {
                    throw new UnKnowException("部署失敗 verify 不通過");
                }
            }
            this.removeOldService(backupResult.getServiceInfoResultList());
        } catch (Exception e) {
            e.printStackTrace();
            this.rollback(serviceDeploy, backupResult, deployResultList);
        }
        appDeploymentResultCallback.setStatusCode("1");
        appDeploymentResultCallback.setMessage("應用程式 " + serviceId + ":" + tag + " 發佈成功, 共 " + deployResultList.size() + " 個應用運行中");
        this.resultCallback(serviceDeploy, appDeploymentResultCallback);
    }

    public Integer getRandomIntegerBetweenRange(int min, int max) {
        Double x = (Math.random() * ((max - min) + 1)) + min;
        return x.intValue();
    }

    public void resultCallback(ServiceDeploy serviceDeploy, AppDeploymentResultCallback appDeploymentResultCallback) {
        String resultCallbackUrl = null;
        try {
            if (appDeploymentResultCallback.getResultCallbackUrl() != null) {
                resultCallbackUrl = appDeploymentResultCallback.getResultCallbackUrl();
            } else {
                resultCallbackUrl = serviceDeploy.getResultCallbackUrl();
            }
            log.info("resultCallbackUrl={}, AppDeploymentResultCallback={}", resultCallbackUrl, appDeploymentResultCallback);
            if (resultCallbackUrl != null) {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                HttpEntity<AppDeploymentResultCallback> entity = new HttpEntity<>(appDeploymentResultCallback, headers);
                ResponseEntity<String> response = restTemplate.exchange(
                        resultCallbackUrl, HttpMethod.POST, entity, String.class);
                log.info("response.getStatusCodeValue={}", response.getStatusCodeValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeOldService(List<ServiceInfoResult> serviceInfoResultList) {
        serviceInfoResultList.forEach(serviceInfoResult -> {
            DockerUtil dockerUtil = DockerUtil.builder().dockerApi(serviceInfoResult.getDockerApi()).build();
            dockerUtil.stopContainerById(serviceInfoResult.getContainerId());
        });
    }

    public BackupResult backupInfo(ServiceDeploy serviceDeploy) {
        BackupResult backupResult = new BackupResult();
        try {
            NginxUpstream nginxUpstream = serviceDeploy.getDeployment().getPostDeploy().getNginxUpstream();
            ShellUtil shellUtil = ShellUtil.builder().host(nginxUpstream.getHost())
                    .port(nginxUpstream.getPort())
                    .username(nginxUpstream.getUsername())
                    .privateKey(nginxUpstream.getPrivateKey())
                    .build();
            Path localTempFile = Files.createTempFile("nginx_" + nginxUpstream.getUpstreamName(), null);
            //Path localTempFile = Paths.get("/test/nginx_" + nginxUpstream.getUpstreamName());
            shellUtil.scpFrom(
                    serviceDeploy.getDeployment().getPostDeploy().getNginxUpstream().getFilePath(),
                    localTempFile.toFile().getAbsolutePath());
            //Path path = Paths.get(serviceDeploy.getDeployment().getPostDeploy().getNginxUpstream().getFilePath());
            String confContent = new String(Files.readAllBytes(localTempFile));
            List<ServiceInfoResult> serviceInfoResultList = this.findOldService(serviceDeploy);
            //
            backupResult.setRemoteConfFilePath(serviceDeploy.getDeployment().getPostDeploy().getNginxUpstream().getFilePath());
            backupResult.setLocalTempConfFilePath(localTempFile.toFile().getAbsolutePath());
            backupResult.setConfContent(confContent);
            backupResult.setServiceInfoResultList(serviceInfoResultList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return backupResult;
    }

    public void rollback(ServiceDeploy serviceDeploy, BackupResult backupResult, List<DeployResult> deployResultList) {
        NginxUpstream nginxUpstream = serviceDeploy.getDeployment().getPostDeploy().getNginxUpstream();
        ShellUtil shellUtil = ShellUtil.builder().host(nginxUpstream.getHost())
                .port(nginxUpstream.getPort())
                .username(nginxUpstream.getUsername())
                .privateKey(nginxUpstream.getPrivateKey())
                .build();
        Path localTempFile = Paths.get(backupResult.getLocalTempConfFilePath());
        String remoteTempFilePath = "/tmp/" + localTempFile.toFile().getName();
        ShellExecResult shellExecResult = null;
        shellExecResult = shellUtil.scpTo(localTempFile.toFile().getAbsolutePath(), remoteTempFilePath);
        shellExecResult = shellUtil.exec("sudo mv " + remoteTempFilePath + " " + nginxUpstream.getFilePath());
        if (shellExecResult.getExitStatus().intValue() == 0) {
            shellExecResult = shellUtil.exec("sudo nginx -t");
            System.out.println(shellExecResult);
        }
    }

    public void postDeploy(ServiceDeploy serviceDeploy, List<ServiceInfoResult> oldServiceList, List<DeployResult> deployResultList) {
        NginxUpstream nginxUpstream = serviceDeploy.getDeployment().getPostDeploy().getNginxUpstream();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("upstream " + nginxUpstream.getUpstreamName() + " {\r\n");
            for (DeployResult deployResult : deployResultList) {
                String serviceIpaddr = deployResult.getIpaddr().equals(nginxUpstream.getHost()) ? "127.0.0.1" : deployResult.getIpaddr();
                sb.append("    server " + serviceIpaddr + ":" + deployResult.getPort() + ";\r\n");
            }
            sb.append("}\r\n");
            Path localTempFile = Files.createTempFile("nginx_" + nginxUpstream.getUpstreamName(), null);
            try (BufferedWriter writer = Files.newBufferedWriter(localTempFile)) {
                writer.write(sb.toString());
            }
            log.info("localTempFile={}", localTempFile.toFile().getAbsoluteFile());
            //Use try-with-resource to get auto-closeable writer instance
            ShellUtil shellUtil = ShellUtil.builder().host(nginxUpstream.getHost())
                    .port(nginxUpstream.getPort())
                    .username(nginxUpstream.getUsername())
                    .privateKey(nginxUpstream.getPrivateKey())
                    .build();
            ShellExecResult shellExecResult = null;
            String tmpFilePath = "/tmp/" + localTempFile.toFile().getName();
            shellExecResult = shellUtil.scpTo(localTempFile.toFile().getAbsolutePath(), tmpFilePath);
            log.info("tmpFilePath={}", tmpFilePath);
            log.info("scpTo shellExecResult={}", shellExecResult);
            shellExecResult = shellUtil.exec("sudo mv " + tmpFilePath + " " + nginxUpstream.getFilePath());
            log.info("sudo mv " + tmpFilePath + " " + nginxUpstream.getFilePath()+" shellExecResult={}", shellExecResult);
            if (shellExecResult.getExitStatus().intValue() == 0) {
                shellExecResult = shellUtil.exec("sudo nginx -t");
                log.info("sudo nginx -t shellExecResult={}", shellExecResult);
            }
            if (shellExecResult.getExitStatus().intValue() == 0) {
                shellExecResult = shellUtil.exec("sudo service nginx reload");
                log.info("sudo service nginx reload shellExecResult={}", shellExecResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean verify(DeployResult deployResult, Verify verify) {
        Boolean verifyResult = false;
        Long timeOutLimit = Integer.valueOf(verify.getTimeout().replace("s", "")) * 1000L;
        String urlFormat = verify.getCurl();
        SpelExpressionParser parser = new SpelExpressionParser();
        Expression message = parser.parseExpression(urlFormat, ParserContext.TEMPLATE_EXPRESSION);
        String verifyUrl = message.getValue(deployResult, String.class);
        Long startTime = System.currentTimeMillis();
        while (verifyResult == false) {
            Long nowTime = System.currentTimeMillis();
            if ((nowTime - startTime) >= timeOutLimit) {
                break;
            }
            try {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.exchange(verifyUrl, HttpMethod.GET, null, String.class);
                if (response.getStatusCodeValue() == 200) {
                    verifyResult = true;
                } else {
                    Thread.sleep(1000);
                }
            } catch (HttpClientErrorException | ResourceAccessException e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return verifyResult;
    }

    public DeployResult deploy(ServiceDeploy serviceDeploy, String tag) {
        Deployment deployment = serviceDeploy.getDeployment();
        List<String> cmdList = new ArrayList<>();
        Integer randomProtNumber = this.getRandomIntegerBetweenRange(49152, 65535);
        cmdList.add("--server.port=" + randomProtNumber);
        deployment.getTemplate().getSpec().getContainers().getCmd().forEach(cmd -> {
            cmdList.add(cmd);
        });
        Map<String, String> labelList = new HashMap<>();
        labelList.put("serviceId", serviceDeploy.getServiceId());
        labelList.put("tag", tag);
        labelList.put("network", deployment.getTemplate().getSpec().getContainers().getNetwork());
        labelList.put("port", String.valueOf(randomProtNumber));
        deployment.getTemplate().getMetadata().getLabels().forEach((key, value) -> {
            labelList.put(key, value);
        });
        List<Bind> bindList = new ArrayList<>();
        deployment.getTemplate().getSpec().getContainers().getVolume().forEach(volume -> {
            bindList.add(Bind.parse(volume));
        });
//        bindList.add(new Bind("/usr/local/app/askask-frontend", new Volume("/askask")));

        Target target = this.selectTarget(serviceDeploy.getDeployment().getTarget());
        DockerUtil dockerUtil = DockerUtil.builder().dockerApi(target.getDockerApi()).build();

        String image = deployment.getTemplate().getSpec().getContainers().getImage() + ":" + tag;
        CreateContainerResponse createContainerResponse = dockerUtil.createAndStartContainer(
                image,
                deployment.getTemplate().getSpec().getContainers().getNetwork(),
                labelList, bindList, cmdList);
        DeployResult deployResult = new DeployResult();
        deployResult.setDockerApi(target.getDockerApi());
        deployResult.setContainerId(createContainerResponse.getId());
        deployResult.setIpaddr(target.getIpaddr());
        deployResult.setPort(randomProtNumber);
        return deployResult;

    }

    // 決策選一台
    private Target selectTarget(List<Target> targetList) {
        Target target = null;
        if (targetList.size() == 1) {
            target = targetList.get(0);
        }
        return target;
    }

    // 要每一台主機去找
    public List<ServiceInfoResult> findOldService(ServiceDeploy serviceDeploy) {
        List<Target> targetList = serviceDeploy.getDeployment().getTarget();
        List<ServiceInfoResult> serviceInfoResultList = new ArrayList<>();
        targetList.forEach(target -> {
            DockerUtil dockerUtil = DockerUtil.builder().dockerApi(target.getDockerApi()).build();
            List<Container> containerList = dockerUtil.findContainerByLabel("serviceId", serviceDeploy.getServiceId());
            containerList.forEach(container -> {
                ServiceInfoResult serviceInfoResult = new ServiceInfoResult();
                serviceInfoResult.setDockerApi(target.getDockerApi());
                serviceInfoResult.setContainerId(container.getId());
                serviceInfoResultList.add(serviceInfoResult);
            });
        });
        return serviceInfoResultList;
    }

}
