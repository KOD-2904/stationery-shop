package org.example.stationery_shop.controller.catalog;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.CatalogImportResponse;
import org.example.stationery_shop.dto.response.ProductImageImportResponse;
import org.example.stationery_shop.service.CatalogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog")
public class CatalogImportController {
    private final CatalogService catalogService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PostMapping("/import")
    public ApiResponse<CatalogImportResponse> importCatalog(
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.<CatalogImportResponse>builder()
                .code(200)
                .message("Imported catalog successfully")
                .result(catalogService.importCatalog(file))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PostMapping("/product-images/import")
    public ApiResponse<ProductImageImportResponse> importProductImages(
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ApiResponse.<ProductImageImportResponse>builder()
                .code(200)
                .message("Imported product images successfully")
                .result(catalogService.importProductImages(file))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PostMapping("/product-images/import/default")
    public ApiResponse<ProductImageImportResponse> importDefaultProductImageFile() {
        return ApiResponse.<ProductImageImportResponse>builder()
                .code(200)
                .message("Imported product images successfully")
                .result(catalogService.importProductImages(null))
                .build();
    }
}
