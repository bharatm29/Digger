package com.bharat.Digger.controller;

import com.bharat.Digger.configuration.ConfigProperties;
import com.bharat.Digger.models.DirObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequiredArgsConstructor
public class DiggerController {
    private final RestTemplate restTemplate;
    private final ConfigProperties config;

    @PostMapping("/show")
    public String showTree(@RequestParam String repo, ModelMap model) {
        model.put("repo", repo);

        String[] splits = repo.split("/+");
        if (splits.length < 4) {
            model.put("error", "Malformed URI: " + repo);
            return "RepoTree";
        }

        final String username = splits[2];
        final String repoName = splits[3];

        final String url = String.format("https://api.github.com/repos/%s/%s/contents", username, repoName);
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", config.getToken()));
        headers.add("Accept", "application/vnd.github.object");
        HttpEntity request = new HttpEntity(headers);

        var response = restTemplate.exchange(url, HttpMethod.GET, request, DirObject.class);

        if(response.getStatusCode() != HttpStatus.OK) {
            model.put("error", "Malformed URI or invalid request: " + url);
            return "RepoTree";
        }

        DirObject root = response.getBody();

        model.put("objs", root.getEntries());

        return "RepoTree";
    }
}