package com.askask.deployment.dto.dockerhub;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "size",
        "architecture",
        "variant",
        "features",
        "os",
        "os_version",
        "os_features"
})
@Data
public class Image {
    @JsonProperty("size")
    private Long size;
    @JsonProperty("architecture")
    private String architecture;
    @JsonProperty("variant")
    private Object variant;
    @JsonProperty("features")
    private Object features;
    @JsonProperty("os")
    private String os; //
    @JsonProperty("os_version")
    private Object osVersion;
    @JsonProperty("os_features")
    private Object osFeatures;
}
