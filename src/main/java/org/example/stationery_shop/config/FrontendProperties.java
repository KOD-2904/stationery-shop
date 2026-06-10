package org.example.stationery_shop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "frontend")
public class FrontendProperties {
    private String baseUrl = "http://localhost:5173/";
    private List<String> allowedOrigins = List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://192.168.1.7/:*",
            "http://127.0.0.1:*"
    );

    public String getBaseUrl() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:5173/";
        }
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }
}
