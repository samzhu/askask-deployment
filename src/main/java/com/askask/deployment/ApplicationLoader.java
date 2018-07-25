package com.askask.deployment;

import com.askask.deployment.service.DeployService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ApplicationLoader implements CommandLineRunner {
    @Autowired
    private DeployService deployService;

    @Override
    public void run(String... args) throws Exception {
//        AppDeploymentRqDto appDeploymentRqDto = new AppDeploymentRqDto();
//        appDeploymentRqDto.setSequenceId("");
//        appDeploymentRqDto.setServiceId("askask-frontend");
//        appDeploymentRqDto.setTag("29");
//        appDeploymentRqDto.setNotifyId("");
//        appDeploymentRqDto.setResultCallbackUrl("");
//        deployService.appDeploy(appDeploymentRqDto);
    }
}
