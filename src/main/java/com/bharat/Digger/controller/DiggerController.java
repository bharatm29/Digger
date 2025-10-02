package com.bharat.Digger.controller;

import com.bharat.Digger.configuration.ConfigProperties;
import com.bharat.Digger.models.DirObject;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

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

        var root = fetchRepo(url);
        if (root == null) {
            model.put("error", "Malformed url: " + url);
            return "RepoTree";
        }

        model.put("root", url);
        model.put("objs", root.getEntries());

        return "RepoTree";
    }

    @PostMapping("/download")
    public String downloadShi(
            @RequestParam String name,
            @RequestParam String download_url,
            @RequestParam String url,
            @RequestParam String type,
            HttpServletResponse response,
            ModelMap model
    ) throws IOException {
        if ("dir".equals(type)) {
            var root = fetchRepo(url);
            if (root == null) {
                model.put("error", "Malformed url: " + url);
            } else {
                model.put("root", url);
                model.put("objs", root.getEntries());
            }

            return "RepoTree";
        }

        try (InputStream in = new URL(download_url).openStream()) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");

            OutputStream out = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }

        return null; // don't render jsp
    }

    @PostMapping("/downloadDir")
    public String downloadDir(@RequestParam String url, @RequestParam String repo) {
        // FIXME: Download directory here
        return null;
    }

    private DirObject fetchRepo(String url) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", config.getToken()));
        headers.add("Accept", "application/vnd.github.object");
        HttpEntity request = new HttpEntity(headers);

        var response = restTemplate.exchange(url, HttpMethod.GET, request, DirObject.class);

        if(response.getStatusCode() != HttpStatus.OK) {
            return null;
        }

        return response.getBody();
    }
}