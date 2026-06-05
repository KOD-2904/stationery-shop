package org.example.stationery_shop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "frontend")
public class FrontendProperties {
    private String baseUrl = "http://localhost:5173/";

    public String getBaseUrl() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:5173/";
        }
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }
}
