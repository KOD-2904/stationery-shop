package org.example.stationery_shop.controller.shipping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.service.shipping.GhnClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ghn")
public class GhnLocationController {
    private final GhnClient ghnClient;
    private final ObjectMapper objectMapper;

    @GetMapping("/provinces")
    public ApiResponse<Object> getProvinces() {
        return ApiResponse.builder()
                .code(200)
                .message("Loaded GHN provinces")
                .result(toJsonValue(ghnClient.getProvinces()))
                .build();
    }

    @GetMapping("/districts")
    public ApiResponse<Object> getDistricts(@RequestParam Integer provinceId) {
        return ApiResponse.builder()
                .code(200)
                .message("Loaded GHN districts")
                .result(toJsonValue(ghnClient.getDistricts(provinceId)))
                .build();
    }

    @GetMapping("/wards")
    public ApiResponse<Object> getWards(@RequestParam Integer districtId) {
        return ApiResponse.builder()
                .code(200)
                .message("Loaded GHN wards")
                .result(toJsonValue(ghnClient.getWards(districtId)))
                .build();
    }

    private Object toJsonValue(JsonNode node) {
        return objectMapper.convertValue(node, Object.class);
    }
}
