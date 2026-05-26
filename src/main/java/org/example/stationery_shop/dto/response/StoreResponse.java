package org.example.stationery_shop.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreResponse {
    private String id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private boolean active;
}
