package org.example.stationery_shop.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CloudinaryProperties {
    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${cloudinary.image-width:1000}")
    private int imageWidth;

    @Value("${cloudinary.image-height:1000}")
    private int imageHeight;
}
