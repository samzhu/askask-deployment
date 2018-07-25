package com.askask.deployment.dto.deploy.conf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "image",
        "network",
        "volume",
        "cmd"
})
@Data
public class Containers {
    @JsonProperty("image")
    private String image;
    @JsonProperty("network")
    private String network;
    @JsonProperty("volume")
    private List<String> volume = null;
    @JsonProperty("cmd")
    private List<String> cmd = null;
}
