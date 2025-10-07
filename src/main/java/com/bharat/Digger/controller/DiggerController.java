package com.bharat.Digger.controller;

import com.bharat.Digger.configuration.ConfigProperties;
import com.bharat.Digger.models.DirObject;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;

import static com.bharat.Digger.configuration.DiggerStrings.*;

@Controller
@RequiredArgsConstructor
@Log4j2
public class DiggerController {
    private final RestTemplate restTemplate;
    private final ConfigProperties config;

    @PostMapping("/show")
    public String showTree(
            @RequestParam(required = false) String repo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String download_url,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String type,
            HttpServletResponse response,
            ModelMap model
    ) throws IOException {
        if (type != null && type.equals("file")) { // handle file download
            return downloadFile(download_url, name, response);
        }

        if (repo != null) { // root repo
            model.put(REPO, repo);

            String[] splits = repo.split("/+");
            if (splits.length < 4) {
                model.put(ERROR, "Malformed URI: " + repo);
                return REPO_TREE;
            }

            final String username = splits[2];
            final String repoName = splits[3];

            url = String.format(GITHUB_FORMAT, username, repoName);
            model.put(REPONAME, repoName);
        }

        // common dir function
        var root = fetchRepo(url);
        if (root == null) {
            model.put(ERROR, "Malformed url: " + url);
            return REPO_TREE;
        }

        model.put(ROOT, url);
        model.put(OBJS, root.getEntries());
        model.put(DIRNAME, root.getPath());

        return REPO_TREE;
    }

    @PostMapping("/downloaddir")
    public String downloadDir(@RequestParam String url, @RequestParam(required = false, defaultValue = "") String reponame, HttpServletResponse response) throws IOException {
        final var dir = fetchRepo(url);
        assert dir != null;

        String path = config.getTempPath();
        if (dir.getPath().isEmpty() && dir.getName().isEmpty()) {
            path += reponame + "/";
        }

        recurseDownload(path, dir);

        if (!dir.getPath().isEmpty()) {
            path += dir.getName();
        }

        final String root = dir.getName().isEmpty() ? reponame : dir.getName();
        final String zipPath = config.getTempPath() + "%s.zip".formatted(root);

        ZipUtil.pack(new File(path), new File(zipPath), name -> root + "/" + name);

        try (InputStream in = new FileInputStream(zipPath)) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + root + ".zip" + "\"");

            OutputStream out = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not find zip file: " + zipPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileUtils.deleteDirectory(new File(path));
            Files.delete(new File(zipPath).toPath());
        } catch (Exception e) {
            log.error("Could not delete directory {}: {}", path, e.getMessage());
        }

        return null; // don't render jsp
    }

    private void recurseDownload(final String path, DirObject dir) throws IOException {
        if (dir == null) return;

        String newPath = path + dir.getName() + "/";
        new File(newPath).mkdirs(); // FIXME: This may throw if it does not have required permission, handle

        for (var file : dir.getEntries()) {
            if (file.getType().equals("file")) {
                try (InputStream in = URI.create(file.getDownload_url()).toURL().openStream()) {
                    OutputStream out = new FileOutputStream(newPath + file.getName());
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                var dirr = fetchRepo(file.getUrl());
                assert dirr != null;
                recurseDownload(newPath, dirr);
            }
        }
    }

    public String downloadFile(String download_url, String name, HttpServletResponse response) throws IOException {
        try (InputStream in = URI.create(download_url).toURL().openStream()) {
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

    // FIXME: Function returns null if it couldn't fetch the dir/repo
    private DirObject fetchRepo(String url) throws IOException {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", config.getToken()));
        headers.add("Accept", "application/vnd.github.object");
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, request, DirObject.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                return null;
            }

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Couldn't fetch contents of {}: {}", url, e.getMessage());
            throw new IOException(e.getMessage()); // FIXME: For now just throwing IOException to centralised error handler
        }
    }
}