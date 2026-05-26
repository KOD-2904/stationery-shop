package org.example.stationery_shop.mapper;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.response.BrandResponse;
import org.example.stationery_shop.dto.response.CategoryResponse;
import org.example.stationery_shop.dto.response.ProductImageResponse;
import org.example.stationery_shop.dto.response.ProductResponse;
import org.example.stationery_shop.dto.response.ProductVariantImageResponse;
import org.example.stationery_shop.dto.response.ProductVariantResponse;
import org.example.stationery_shop.entity.catalog.Brand;
import org.example.stationery_shop.entity.catalog.Category;
import org.example.stationery_shop.entity.catalog.Product;
import org.example.stationery_shop.entity.catalog.ProductImage;
import org.example.stationery_shop.entity.catalog.ProductVariant;
import org.example.stationery_shop.entity.catalog.ProductVariantImage;
import org.example.stationery_shop.repository.ProductImageRepository;
import org.example.stationery_shop.repository.ProductVariantImageRepository;
import org.example.stationery_shop.repository.ProductVariantRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogMapper {
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantImageRepository productVariantImageRepository;

    public BrandResponse toBrandResponse(Brand brand) {
        if (brand == null) {
            return null;
        }
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .active(brand.isActive())
                .build();
    }

    public CategoryResponse toCategoryResponse(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .active(category.isActive())
                .build();
    }

    public ProductResponse toProductResponse(Product product) {
        if (product == null) {
            return null;
        }
        List<ProductVariantResponse> variants = productVariantRepository
                .findByProductIdOrderBySizeAsc(product.getId())
                .stream()
                .map(this::toProductVariantResponse)
                .toList();

        List<ProductImageResponse> images = productImageRepository
                .findByProductIdOrderBySortOrderAscCreatedAtAsc(product.getId())
                .stream()
                .map(this::toProductImageResponse)
                .toList();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .brand(toBrandResponse(product.getBrand()))
                .category(toCategoryResponse(product.getCategory()))
                .material(product.getMaterial())
                .goldAge(product.getGoldAge())
                .stoneType(product.getStoneType())
                .thumbnailUrl(product.getThumbnailUrl())
                .pricingType(product.getPricingType())
                .active(product.isActive())
                .variants(variants)
                .images(images)
                .build();
    }

    public ProductVariantResponse toProductVariantResponse(ProductVariant variant) {
        if (variant == null) {
            return null;
        }
        List<ProductVariantImageResponse> images = productVariantImageRepository
                .findByProductVariantIdOrderBySortOrderAscCreatedAtAsc(variant.getId())
                .stream()
                .map(this::toProductVariantImageResponse)
                .toList();

        return ProductVariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                .sku(variant.getSku())
                .size(variant.getSize())
                .price(variant.getPrice())
                .goldWeight(variant.getGoldWeight())
                .laborCost(variant.getLaborCost())
                .imageUrl(variant.getImageUrl())
                .active(variant.isActive())
                .version(variant.getVersion())
                .images(images)
                .build();
    }

    public ProductImageResponse toProductImageResponse(ProductImage image) {
        if (image == null) {
            return null;
        }
        return ProductImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .primaryImage(image.isPrimaryImage())
                .sortOrder(image.getSortOrder())
                .build();
    }

    public ProductVariantImageResponse toProductVariantImageResponse(ProductVariantImage image) {
        if (image == null) {
            return null;
        }
        return ProductVariantImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .primaryImage(image.isPrimaryImage())
                .sortOrder(image.getSortOrder())
                .build();
    }
}
