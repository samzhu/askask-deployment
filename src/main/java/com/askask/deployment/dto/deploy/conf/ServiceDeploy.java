package com.askask.deployment.dto.deploy.conf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "service-id",
        "deployment",
        "result-callback-url"
})
@Data
public class ServiceDeploy {
    @JsonProperty("service-id")
    private String serviceId;
    @JsonProperty("deployment")
    private Deployment deployment;
    @JsonProperty("result-callback-url")
    private String resultCallbackUrl;
}
