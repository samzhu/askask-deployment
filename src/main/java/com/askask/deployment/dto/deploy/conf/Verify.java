package com.askask.deployment.dto.deploy.conf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "curl",
        "timeout"
})
@Data
public class Verify {
    @JsonProperty("curl")
    private String curl;
    @JsonProperty("timeout")
    private String timeout;
}
