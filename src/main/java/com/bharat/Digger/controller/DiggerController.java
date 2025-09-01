package com.bharat.Digger.controller;

import com.bharat.Digger.configuration.ConfigProperties;
import lombok.RequiredArgsConstructor;
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
        model.put("token", config.getToken());

        return "RepoTree";
    }
}