package com.askask.deployment.dto.deploy.conf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "metadata",
        "spec"
})
@Data
public class Template {
    @JsonProperty("metadata")
    private Metadata metadata;
    @JsonProperty("spec")
    private Spec spec;
}
