package com.bharat.Digger.controller;

import com.bharat.Digger.services.DiggerService;
import com.bharat.Digger.configuration.ConfigProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.*;
import java.nio.file.Files;

import static com.bharat.Digger.configuration.DiggerStrings.*;

@Controller
@RequiredArgsConstructor
@Log4j2
public class DiggerController {
    private final DiggerService service;
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
            return service.downloadFile(download_url, name, response);
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
        var root = service.fetchRepo(url);
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
    public String downloadDir(
            @RequestParam String url,
            @RequestParam(required = false, defaultValue = "") String reponame,
            HttpServletResponse response
    ) throws IOException {
        final var dir = service.fetchRepo(url);

        String path = config.getTempPath();
        if (dir.getPath().isEmpty() && dir.getName().isEmpty()) {
            path += reponame + "/";
        }

        service.recurseDownload(path, dir);

        if (!dir.getPath().isEmpty()) {
            path += dir.getName();
        }

        // zip downloaded folder, send over response and delete it
        final String root = dir.getName().isEmpty() ? reponame : dir.getName();
        final String zipPath = config.getTempPath() + "%s.zip".formatted(root);

        ZipUtil.pack(new File(path), new File(zipPath), name -> root + "/" + name);

        try {
            service.dispatchFileToResponse(new FileInputStream(zipPath), response, root + ".zip");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not find zip file: " + zipPath);
        }

        try {
            FileUtils.deleteDirectory(new File(path));
            Files.delete(new File(zipPath).toPath());
        } catch (Exception e) {
            log.error("Could not delete directory {}: {}", path, e.getMessage());
        }

        return null; // don't render jsp
    }
}