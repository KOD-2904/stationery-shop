package org.example.stationery_shop.controller.inventory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.StoreRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.StoreResponse;
import org.example.stationery_shop.service.InventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {
    private final InventoryService inventoryService;

    @GetMapping
    public ApiResponse<List<StoreResponse>> getStores(
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ApiResponse.<List<StoreResponse>>builder()
                .code(200)
                .message("Success")
                .result(inventoryService.getStores(includeInactive))
                .build();
    }

    @PostMapping
    public ApiResponse<StoreResponse> createStore(@Valid @RequestBody StoreRequest request) {
        return ApiResponse.<StoreResponse>builder()
                .code(200)
                .message("Created store successfully")
                .result(inventoryService.createStore(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<StoreResponse> updateStore(
            @PathVariable String id,
            @Valid @RequestBody StoreRequest request
    ) {
        return ApiResponse.<StoreResponse>builder()
                .code(200)
                .message("Updated store successfully")
                .result(inventoryService.updateStore(id, request))
                .build();
    }
}
