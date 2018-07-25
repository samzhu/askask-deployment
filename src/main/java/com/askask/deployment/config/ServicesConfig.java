package com.askask.deployment.config;

import com.askask.deployment.dto.deploy.conf.ServiceDeploy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 管理的服務列表
 */
@Data
@Component
@ConfigurationProperties
public class ServicesConfig {
    private List<ServiceDeploy> services;
}
