package com.bharat.Digger.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "digger")
@Data
public class ConfigProperties {
    private String token;
    private String tempPath;
}
