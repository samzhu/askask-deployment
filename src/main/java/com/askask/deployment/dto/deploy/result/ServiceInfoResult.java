package com.askask.deployment.dto.deploy.result;

import lombok.Data;

@Data
public class ServiceInfoResult {
    private String dockerApi;
    private String containerId;
}
