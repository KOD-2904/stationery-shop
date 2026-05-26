package org.example.stationery_shop.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private String id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private boolean active;
}
