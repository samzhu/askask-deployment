package com.askask.deployment.dto.delivery;

import lombok.Data;

@Data
public class AppDeploymentRqDto {
    private String sequenceId;// 請求序號
    private String serviceId;
    private String tag;
    private String notifyId;
    private String resultCallbackUrl;
}
