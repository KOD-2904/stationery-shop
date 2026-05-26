package org.example.stationery_shop.controller.inventory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.InventoryAdjustRequest;
import org.example.stationery_shop.dto.request.InventoryChangeRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.InventoryResponse;
import org.example.stationery_shop.service.InventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/variants/{productVariantId}")
    public ApiResponse<List<InventoryResponse>> getInventoryByVariant(
            @PathVariable String productVariantId
    ) {
        return ApiResponse.<List<InventoryResponse>>builder()
                .code(200)
                .message("Success")
                .result(inventoryService.getInventoryByVariant(productVariantId))
                .build();
    }

    @PostMapping("/import")
    public ApiResponse<InventoryResponse> importStock(
            @Valid @RequestBody InventoryChangeRequest request
    ) {
        return ApiResponse.<InventoryResponse>builder()
                .code(200)
                .message("Imported stock successfully")
                .result(inventoryService.importStock(request))
                .build();
    }

    @PostMapping("/adjust")
    public ApiResponse<InventoryResponse> adjustStock(
            @Valid @RequestBody InventoryAdjustRequest request
    ) {
        return ApiResponse.<InventoryResponse>builder()
                .code(200)
                .message("Adjusted stock successfully")
                .result(inventoryService.adjustStock(request))
                .build();
    }

    @PostMapping("/lock")
    public ApiResponse<InventoryResponse> lockStock(
            @Valid @RequestBody InventoryChangeRequest request
    ) {
        return ApiResponse.<InventoryResponse>builder()
                .code(200)
                .message("Locked stock successfully")
                .result(inventoryService.lockStock(request))
                .build();
    }

    @PostMapping("/release")
    public ApiResponse<InventoryResponse> releaseStock(
            @Valid @RequestBody InventoryChangeRequest request
    ) {
        return ApiResponse.<InventoryResponse>builder()
                .code(200)
                .message("Released stock successfully")
                .result(inventoryService.releaseStock(request))
                .build();
    }

    @PostMapping("/deduct")
    public ApiResponse<InventoryResponse> deductStock(
            @Valid @RequestBody InventoryChangeRequest request
    ) {
        return ApiResponse.<InventoryResponse>builder()
                .code(200)
                .message("Deducted stock successfully")
                .result(inventoryService.deductStock(request))
                .build();
    }
}
