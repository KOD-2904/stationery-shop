package org.example.stationery_shop.controller.catalog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.ProductVariantRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.ProductVariantResponse;
import org.example.stationery_shop.dto.response.ProductVariantImageResponse;
import org.example.stationery_shop.service.CatalogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class ProductVariantController {
    private final CatalogService catalogService;

    @PostMapping("/products/{productId}/variants")
    public ApiResponse<ProductVariantResponse> createVariant(
            @PathVariable String productId,
            @Valid @RequestBody ProductVariantRequest request
    ) {
        return ApiResponse.<ProductVariantResponse>builder()
                .code(200)
                .message("Created product variant successfully")
                .result(catalogService.createVariant(productId, request))
                .build();
    }

    @PutMapping("/product-variants/{id}")
    public ApiResponse<ProductVariantResponse> updateVariant(
            @PathVariable String id,
            @Valid @RequestBody ProductVariantRequest request
    ) {
        return ApiResponse.<ProductVariantResponse>builder()
                .code(200)
                .message("Updated product variant successfully")
                .result(catalogService.updateVariant(id, request))
                .build();
    }

    @PostMapping("/product-variants/{id}/main-image")
    public ApiResponse<ProductVariantResponse> uploadMainImage(
            @PathVariable String id,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.<ProductVariantResponse>builder()
                .code(200)
                .message("Uploaded product variant image successfully")
                .result(catalogService.uploadVariantMainImage(id, file))
                .build();
    }

    @PostMapping("/product-variants/{id}/images")
    public ApiResponse<ProductVariantImageResponse> uploadImage(
            @PathVariable String id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean primaryImage,
            @RequestParam(required = false) Integer sortOrder
    ) {
        return ApiResponse.<ProductVariantImageResponse>builder()
                .code(200)
                .message("Uploaded product variant image successfully")
                .result(catalogService.uploadVariantImage(id, file, primaryImage, sortOrder))
                .build();
    }
}
