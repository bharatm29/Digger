package com.bharat.Digger.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "credentials")
@Data
public class ConfigProperties {
    private String token;
}
