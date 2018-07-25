package com.askask.deployment.utils;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import lombok.Builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
public class DockerUtil {
    private String dockerApi;

    public CreateContainerResponse createAndStartContainer(String image, String networkMode, Map<String, String> labels, List<Bind> binds, List<String> cmd) {
        CreateContainerResponse createContainerResponse = null;
        try {
            DockerClient dockerClient = DockerClientBuilder.getInstance(this.dockerApi).build();

            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback();
            pullImageResultCallback = dockerClient.pullImageCmd(image).exec(pullImageResultCallback);
            pullImageResultCallback.awaitCompletion();
//                    .awaitCompletion(60, TimeUnit.SECONDS);
            createContainerResponse
                    = dockerClient.createContainerCmd(image)
                    .withNetworkMode(networkMode)
                    .withLabels(labels)
                    .withBinds(binds)
                    .withCmd(cmd)
                    .exec();
            dockerClient.startContainerCmd(createContainerResponse.getId()).exec();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createContainerResponse;
    }

    public List<Container> findContainerByLabel(String key, String value) {
        DockerClient dockerClient = DockerClientBuilder.getInstance(this.dockerApi).build();
        Map<String, String> labels = new HashMap<>();
        labels.put(key, value);
        List<Container> containerList = dockerClient.listContainersCmd().withLabelFilter(labels).exec();
        return containerList;
    }

    public void stopContainerById(String containerId) {
        DockerClient dockerClient = DockerClientBuilder.getInstance(this.dockerApi).build();
        dockerClient.stopContainerCmd(containerId).exec();
    }

    public void removeContainerById(String containerId) {
        DockerClient dockerClient = DockerClientBuilder.getInstance(this.dockerApi).build();
        dockerClient.removeContainerCmd(containerId).exec();
    }

    public void removeImageById(String imageId) {
        DockerClient dockerClient = DockerClientBuilder.getInstance(this.dockerApi).build();
        dockerClient.removeImageCmd(imageId).exec();
    }
}
