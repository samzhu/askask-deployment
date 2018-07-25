package com.askask.deployment.dto.deploy.conf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "host",
        "port",
        "username",
        "private-key",
        "upstream-name",
        "file-path"
})
@Data
public class NginxUpstream {
    @JsonProperty("host")
    private String host;
    @JsonProperty("port")
    private Integer port;
    @JsonProperty("username")
    private String username;
    @JsonProperty("private-key")
    private String privateKey;
    @JsonProperty("upstream-name")
    private String upstreamName;
    @JsonProperty("file-path")
    private String filePath;
}
