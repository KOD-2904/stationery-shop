package org.example.stationery_shop.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandResponse {
    private String id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private boolean active;
}
