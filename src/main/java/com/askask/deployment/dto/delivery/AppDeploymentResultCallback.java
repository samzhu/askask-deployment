package com.askask.deployment.dto.delivery;

import lombok.Data;

@Data
public class AppDeploymentResultCallback {
    private String sequenceId;
    private String statusCode;
    private String message;
    private String serviceId;
    private String tag;
    private String notifyId;
    private String resultCallbackUrl;
}
