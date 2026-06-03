package org.example.stationery_shop.controller.catalog;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.ProductVariantImageImportRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.ProductVariantImageImportJobResponse;
import org.example.stationery_shop.service.ProductVariantImageImportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog/product-variant-images/import")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class ProductVariantImageImportController {
    private final ProductVariantImageImportService productVariantImageImportService;

    @PostMapping
    public ApiResponse<ProductVariantImageImportJobResponse> startImport(
            @RequestBody(required = false) ProductVariantImageImportRequest request
    ) {
        return ApiResponse.<ProductVariantImageImportJobResponse>builder()
                .code(202)
                .message("Product variant image import job started")
                .result(productVariantImageImportService.startImport(request))
                .build();
    }

    @GetMapping("/{jobId}")
    public ApiResponse<ProductVariantImageImportJobResponse> getJob(
            @PathVariable String jobId
    ) {
        return ApiResponse.<ProductVariantImageImportJobResponse>builder()
                .code(200)
                .message("Success")
                .result(productVariantImageImportService.getJob(jobId))
                .build();
    }
}
