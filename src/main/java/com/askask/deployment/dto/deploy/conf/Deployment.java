package com.askask.deployment.dto.deploy.conf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "replicas",
        "template",
        "target",
        "verify",
        "post-deploy"
})
@Data
public class Deployment {
    @JsonProperty("replicas")
    private Integer replicas;
    @JsonProperty("template")
    private Template template;
    @JsonProperty("target")
    private List<Target> target = null;
    @JsonProperty("verify")
    private Verify verify;
    @JsonProperty("post-deploy")
    private PostDeploy postDeploy;
}
