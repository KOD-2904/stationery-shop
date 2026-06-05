package org.example.stationery_shop.controller.catalog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.BrandRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.BrandResponse;
import org.example.stationery_shop.service.CatalogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brands")
public class BrandController {
    private final CatalogService catalogService;

    @GetMapping
    public ApiResponse<List<BrandResponse>> getBrands(
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ApiResponse.<List<BrandResponse>>builder()
                .code(200)
                .message("Success")
                .result(catalogService.getBrands(includeInactive))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PostMapping
    public ApiResponse<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        return ApiResponse.<BrandResponse>builder()
                .code(200)
                .message("Created brand successfully")
                .result(catalogService.createBrand(request))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PutMapping("/{id}")
    public ApiResponse<BrandResponse> updateBrand(
            @PathVariable String id,
            @Valid @RequestBody BrandRequest request
    ) {
        return ApiResponse.<BrandResponse>builder()
                .code(200)
                .message("Updated brand successfully")
                .result(catalogService.updateBrand(id, request))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PatchMapping("/{id}/active")
    public ApiResponse<BrandResponse> updateBrandActive(
            @PathVariable String id,
            @RequestParam boolean active
    ) {
        return ApiResponse.<BrandResponse>builder()
                .code(200)
                .message("Updated brand status successfully")
                .result(catalogService.updateBrandActive(id, active))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PostMapping("/{id}/logo")
    public ApiResponse<BrandResponse> uploadLogo(
            @PathVariable String id,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.<BrandResponse>builder()
                .code(200)
                .message("Uploaded brand logo successfully")
                .result(catalogService.uploadBrandLogo(id, file))
                .build();
    }
}
