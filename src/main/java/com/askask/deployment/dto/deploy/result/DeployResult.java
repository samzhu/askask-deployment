package com.askask.deployment.dto.deploy.result;

import lombok.Data;

@Data
public class DeployResult {
    private String containerId;
    private String dockerApi;
    private String ipaddr;
    private Integer port;
}
