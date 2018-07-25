package com.askask.deployment.dto.deploy.result;

import lombok.Data;

import java.util.List;

@Data
public class BackupResult {
    private String remoteConfFilePath;
    private String localTempConfFilePath;
    private String confContent;
    private List<ServiceInfoResult> serviceInfoResultList;
}
