package com.askask.deployment.dto.dockerhub;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "full_size",
        "images",
        "id",
        "repository",
        "creator",
        "last_updater",
        "last_updated",
        "image_id",
        "v2"
})
@Data
public class Result {
    @JsonProperty("name")
    private String name;
    @JsonProperty("full_size")
    private Integer fullSize;
    @JsonProperty("images")
    private List<Image> images = null;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("repository")
    private Integer repository;
    @JsonProperty("creator")
    private Integer creator;
    @JsonProperty("last_updater")
    private Integer lastUpdater;
    @JsonProperty("last_updated")
    private String lastUpdated;
    @JsonProperty("image_id")
    private Object imageId;
    @JsonProperty("v2")
    private Boolean v2;
}
