package org.example.stationery_shop.controller.catalog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.CategoryRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.CategoryResponse;
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
@RequestMapping("/api/categories")
public class CategoryController {
    private final CatalogService catalogService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getCategories(
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ApiResponse.<List<CategoryResponse>>builder()
                .code(200)
                .message("Success")
                .result(catalogService.getCategories(includeInactive))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .message("Created category successfully")
                .result(catalogService.createCategory(request))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .message("Updated category successfully")
                .result(catalogService.updateCategory(id, request))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PatchMapping("/{id}/active")
    public ApiResponse<CategoryResponse> updateCategoryActive(
            @PathVariable String id,
            @RequestParam boolean active
    ) {
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .message("Updated category status successfully")
                .result(catalogService.updateCategoryActive(id, active))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @PostMapping("/{id}/image")
    public ApiResponse<CategoryResponse> uploadImage(
            @PathVariable String id,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .message("Uploaded category image successfully")
                .result(catalogService.uploadCategoryImage(id, file))
                .build();
    }
}
