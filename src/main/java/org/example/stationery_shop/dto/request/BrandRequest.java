package org.example.stationery_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandRequest {
    @NotBlank(message = "Brand name is required")
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private Boolean active;
}
