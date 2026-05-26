package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.BrandRequest;
import org.example.stationery_shop.dto.request.CategoryRequest;
import org.example.stationery_shop.dto.request.ProductRequest;
import org.example.stationery_shop.dto.request.ProductVariantRequest;
import org.example.stationery_shop.dto.response.BrandResponse;
import org.example.stationery_shop.dto.response.CategoryResponse;
import org.example.stationery_shop.dto.response.ProductImageResponse;
import org.example.stationery_shop.dto.response.ProductResponse;
import org.example.stationery_shop.dto.response.ProductVariantImageResponse;
import org.example.stationery_shop.dto.response.ProductVariantResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CatalogService {
    List<BrandResponse> getBrands(boolean includeInactive);
    BrandResponse createBrand(BrandRequest request);
    BrandResponse updateBrand(String id, BrandRequest request);
    BrandResponse uploadBrandLogo(String id, MultipartFile file);

    List<CategoryResponse> getCategories(boolean includeInactive);
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(String id, CategoryRequest request);
    CategoryResponse uploadCategoryImage(String id, MultipartFile file);

    List<ProductResponse> getProducts(boolean includeInactive);
    ProductResponse getProduct(String id);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(String id, ProductRequest request);
    ProductResponse uploadProductThumbnail(String id, MultipartFile file);
    ProductImageResponse uploadProductImage(String productId, MultipartFile file, boolean primaryImage, Integer sortOrder);

    ProductVariantResponse createVariant(String productId, ProductVariantRequest request);
    ProductVariantResponse updateVariant(String id, ProductVariantRequest request);
    ProductVariantResponse uploadVariantMainImage(String id, MultipartFile file);
    ProductVariantImageResponse uploadVariantImage(String id, MultipartFile file, boolean primaryImage, Integer sortOrder);
}
