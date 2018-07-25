package com.askask.deployment.dto.deploy.conf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "nginx-upstream"
})
@Data
public class PostDeploy {
    @JsonProperty("nginx-upstream")
    private NginxUpstream nginxUpstream;
}
