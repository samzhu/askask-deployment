package com.askask.deployment.controller;

import com.askask.deployment.dto.delivery.AppDeploymentRqDto;
import com.askask.deployment.service.DeployService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AppRestController {
    @Autowired
    private DeployService deployService;

    /**
     * 發布版本
     * @param appDeploymentRqDto
     */
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    @PostMapping(path = "/app/deployment")
    public void deployment(@RequestBody AppDeploymentRqDto appDeploymentRqDto) {
        deployService.appDeploy(appDeploymentRqDto);
    }
}
