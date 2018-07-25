package com.askask.deployment.dto.deploy.conf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "nodeType",
        "ipaddr",
        "dockerApi"
})
@Data
public class Target {
    @JsonProperty("nodeType")
    private String nodeType;
    @JsonProperty("ipaddr")
    private String ipaddr;
    @JsonProperty("dockerApi")
    private String dockerApi;
}
