package com.askask.deployment.service;

import com.askask.deployment.dto.dockerhub.TagsInfo;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class DockerService {

    /**
     * 取得標籤清單
     *
     * @param user
     * @param image
     * @return
     */
    public TagsInfo getTags(String user, String image) {
        TagsInfo tagsInfo = null;
        try {
            UriComponentsBuilder builder =
                    UriComponentsBuilder.fromHttpUrl("https://hub.docker.com/v2/repositories/{user}/{image}/tags/");
            Map<String, String> uriParams = new HashMap<String, String>();
            uriParams.put("user", user);
            uriParams.put("image", image);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<Object> entity = new HttpEntity<>(null, headers);
            ResponseEntity<TagsInfo> response = restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET, entity, TagsInfo.class);
            tagsInfo = response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tagsInfo;
    }


}
