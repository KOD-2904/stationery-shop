package org.example.stationery_shop.controller.catalog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.ProductRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.ProductImageResponse;
import org.example.stationery_shop.dto.response.ProductResponse;
import org.example.stationery_shop.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/products")
public class ProductController {
    private final CatalogService catalogService;

    @GetMapping
    public ApiResponse<List<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ApiResponse.<List<ProductResponse>>builder()
                .code(200)
                .message("Success")
                .result(catalogService.getProducts(includeInactive))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable String id) {
        return ApiResponse.<ProductResponse>builder()
                .code(200)
                .message("Success")
                .result(catalogService.getProduct(id))
                .build();
    }

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .code(200)
                .message("Created product successfully")
                .result(catalogService.createProduct(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ApiResponse.<ProductResponse>builder()
                .code(200)
                .message("Updated product successfully")
                .result(catalogService.updateProduct(id, request))
                .build();
    }

    @PostMapping("/{id}/thumbnail")
    public ApiResponse<ProductResponse> uploadThumbnail(
            @PathVariable String id,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.<ProductResponse>builder()
                .code(200)
                .message("Uploaded product thumbnail successfully")
                .result(catalogService.uploadProductThumbnail(id, file))
                .build();
    }

    @PostMapping("/{id}/images")
    public ApiResponse<ProductImageResponse> uploadImage(
            @PathVariable String id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean primaryImage,
            @RequestParam(required = false) Integer sortOrder
    ) {
        return ApiResponse.<ProductImageResponse>builder()
                .code(200)
                .message("Uploaded product image successfully")
                .result(catalogService.uploadProductImage(id, file, primaryImage, sortOrder))
                .build();
    }
}
