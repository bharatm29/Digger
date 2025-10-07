package com.bharat.Digger.services;

import com.bharat.Digger.configuration.ConfigProperties;
import com.bharat.Digger.models.DirObject;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URI;

@Service
@RequiredArgsConstructor
@Log4j2
public class DiggerService {
    private final RestTemplate restTemplate;
    private final ConfigProperties config;

    public void recurseDownload(final String path, DirObject dir) throws IOException {
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


    public String downloadFile(
            String download_url,
            String name,
            HttpServletResponse response
    ) throws IOException {
        var in = URI.create(download_url).toURL().openStream();
        dispatchFileToResponse(in, response, name);

        return null; // don't render jsp
    }

    // FIXME: Function returns null if it couldn't fetch the dir/repo
    public DirObject fetchRepo(String url) throws IOException {
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

    public void dispatchFileToResponse(
            InputStream in,
            HttpServletResponse response,
            final String name
    ) throws IOException {
        try (in) {
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
    }
}
