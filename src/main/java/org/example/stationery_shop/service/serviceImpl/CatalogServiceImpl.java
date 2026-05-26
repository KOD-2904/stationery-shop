package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
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
import org.example.stationery_shop.entity.catalog.Brand;
import org.example.stationery_shop.entity.catalog.Category;
import org.example.stationery_shop.entity.catalog.Product;
import org.example.stationery_shop.entity.catalog.ProductImage;
import org.example.stationery_shop.entity.catalog.ProductVariant;
import org.example.stationery_shop.entity.catalog.ProductVariantImage;
import org.example.stationery_shop.enums.PricingType;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.mapper.CatalogMapper;
import org.example.stationery_shop.repository.BrandRepository;
import org.example.stationery_shop.repository.CategoryRepository;
import org.example.stationery_shop.repository.ProductImageRepository;
import org.example.stationery_shop.repository.ProductRepository;
import org.example.stationery_shop.repository.ProductVariantImageRepository;
import org.example.stationery_shop.repository.ProductVariantRepository;
import org.example.stationery_shop.service.CatalogService;
import org.example.stationery_shop.service.CloudinaryService;
import org.example.stationery_shop.utils.SlugUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {
    private static final String CLOUDINARY_ROOT_FOLDER = "Jewelry Shop";

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantImageRepository productVariantImageRepository;
    private final CatalogMapper catalogMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<BrandResponse> getBrands(boolean includeInactive) {
        List<Brand> brands = includeInactive
                ? brandRepository.findAll()
                : brandRepository.findByActiveTrueOrderByNameAsc();
        return brands.stream().map(catalogMapper::toBrandResponse).toList();
    }

    @Override
    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());
        ensureSlugAvailable(brandRepository.existsBySlug(slug));
        Brand brand = Brand.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .active(request.getActive() == null || request.getActive())
                .build();
        return catalogMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(String id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_EXIST));
        String slug = resolveSlug(request.getSlug(), request.getName());
        if (!brand.getSlug().equals(slug) && brandRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.SLUG_EXISTED);
        }
        brand.setName(request.getName());
        brand.setSlug(slug);
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        if (request.getActive() != null) {
            brand.setActive(request.getActive());
        }
        return catalogMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandResponse uploadBrandLogo(String id, MultipartFile file) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_EXIST));
        brand.setLogoUrl(cloudinaryService.uploadImage(file, brandFolder()));
        return catalogMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Override
    public List<CategoryResponse> getCategories(boolean includeInactive) {
        List<Category> categories = includeInactive
                ? categoryRepository.findAll()
                : categoryRepository.findByActiveTrueOrderByNameAsc();
        return categories.stream().map(catalogMapper::toCategoryResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());
        ensureSlugAvailable(categoryRepository.existsBySlug(slug));
        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() == null || request.getActive())
                .build();
        return catalogMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXIST));
        String slug = resolveSlug(request.getSlug(), request.getName());
        if (!category.getSlug().equals(slug) && categoryRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.SLUG_EXISTED);
        }
        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        return catalogMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse uploadCategoryImage(String id, MultipartFile file) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXIST));
        category.setImageUrl(cloudinaryService.uploadImage(file, categoryFolder()));
        return catalogMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public List<ProductResponse> getProducts(boolean includeInactive) {
        List<Product> products = includeInactive
                ? productRepository.findAllByOrderByCreatedAtDesc()
                : productRepository.findByActiveTrueOrderByCreatedAtDesc();
        return products.stream().map(catalogMapper::toProductResponse).toList();
    }

    @Override
    public ProductResponse getProduct(String id) {
        Product product = productRepository.findWithBrandAndCategoryById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        return catalogMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());
        ensureSlugAvailable(productRepository.existsBySlug(slug));
        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .brand(resolveBrand(request.getBrandId()))
                .category(resolveCategory(request.getCategoryId()))
                .material(request.getMaterial())
                .goldAge(request.getGoldAge())
                .stoneType(request.getStoneType())
                .thumbnailUrl(request.getThumbnailUrl())
                .pricingType(request.getPricingType() == null ? PricingType.ABSOLUTE_FIXED : request.getPricingType())
                .active(request.getActive() == null || request.getActive())
                .build();
        Product savedProduct = productRepository.save(product);
        return getProduct(savedProduct.getId());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = productRepository.findWithBrandAndCategoryById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        String slug = resolveSlug(request.getSlug(), request.getName());
        if (!product.getSlug().equals(slug) && productRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.SLUG_EXISTED);
        }
        product.setName(request.getName());
        product.setSlug(slug);
        product.setDescription(request.getDescription());
        product.setBrand(resolveBrand(request.getBrandId()));
        product.setCategory(resolveCategory(request.getCategoryId()));
        product.setMaterial(request.getMaterial());
        product.setGoldAge(request.getGoldAge());
        product.setStoneType(request.getStoneType());
        product.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getPricingType() != null) {
            product.setPricingType(request.getPricingType());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        Product savedProduct = productRepository.save(product);
        return getProduct(savedProduct.getId());
    }

    @Override
    @Transactional
    public ProductResponse uploadProductThumbnail(String id, MultipartFile file) {
        Product product = productRepository.findWithBrandAndCategoryById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        product.setThumbnailUrl(cloudinaryService.uploadImage(file, productFolder(id)));
        productRepository.save(product);
        return getProduct(id);
    }

    @Override
    @Transactional
    public ProductImageResponse uploadProductImage(String productId, MultipartFile file, boolean primaryImage, Integer sortOrder) {
        Product product = productRepository.findWithBrandAndCategoryById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(cloudinaryService.uploadImage(file, productFolder(productId)))
                .primaryImage(primaryImage)
                .sortOrder(sortOrder)
                .build();
        ProductImage savedImage = productImageRepository.save(image);
        if (primaryImage && product.getThumbnailUrl() == null) {
            product.setThumbnailUrl(savedImage.getImageUrl());
            productRepository.save(product);
        }
        return catalogMapper.toProductImageResponse(savedImage);
    }

    @Override
    @Transactional
    public ProductVariantResponse createVariant(String productId, ProductVariantRequest request) {
        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new AppException(ErrorCode.SKU_EXISTED);
        }
        Product product = productRepository.findWithBrandAndCategoryById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(request.getSku())
                .size(request.getSize())
                .price(request.getPrice())
                .goldWeight(request.getGoldWeight())
                .laborCost(request.getLaborCost())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() == null || request.getActive())
                .build();
        return catalogMapper.toProductVariantResponse(productVariantRepository.save(variant));
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariant(String id, ProductVariantRequest request) {
        ProductVariant variant = productVariantRepository.findWithProductById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));
        if (!variant.getSku().equals(request.getSku()) && productVariantRepository.existsBySku(request.getSku())) {
            throw new AppException(ErrorCode.SKU_EXISTED);
        }
        variant.setSku(request.getSku());
        variant.setSize(request.getSize());
        variant.setPrice(request.getPrice());
        variant.setGoldWeight(request.getGoldWeight());
        variant.setLaborCost(request.getLaborCost());
        variant.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            variant.setActive(request.getActive());
        }
        return catalogMapper.toProductVariantResponse(productVariantRepository.save(variant));
    }

    @Override
    @Transactional
    public ProductVariantResponse uploadVariantMainImage(String id, MultipartFile file) {
        ProductVariant variant = productVariantRepository.findWithProductById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));
        variant.setImageUrl(cloudinaryService.uploadImage(file, variantFolder(variant)));
        return catalogMapper.toProductVariantResponse(productVariantRepository.save(variant));
    }

    @Override
    @Transactional
    public ProductVariantImageResponse uploadVariantImage(String id, MultipartFile file, boolean primaryImage, Integer sortOrder) {
        ProductVariant variant = productVariantRepository.findWithProductById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));
        ProductVariantImage image = ProductVariantImage.builder()
                .productVariant(variant)
                .imageUrl(cloudinaryService.uploadImage(file, variantFolder(variant)))
                .primaryImage(primaryImage)
                .sortOrder(sortOrder)
                .build();
        ProductVariantImage savedImage = productVariantImageRepository.save(image);
        if (primaryImage && variant.getImageUrl() == null) {
            variant.setImageUrl(savedImage.getImageUrl());
            productVariantRepository.save(variant);
        }
        return catalogMapper.toProductVariantImageResponse(savedImage);
    }

    private Brand resolveBrand(String brandId) {
        if (brandId == null || brandId.isBlank()) {
            return null;
        }
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_EXIST));
    }

    private Category resolveCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXIST));
    }

    private String resolveSlug(String requestedSlug, String name) {
        return requestedSlug == null || requestedSlug.isBlank()
                ? SlugUtils.toSlug(name)
                : SlugUtils.toSlug(requestedSlug);
    }

    private void ensureSlugAvailable(boolean exists) {
        if (exists) {
            throw new AppException(ErrorCode.SLUG_EXISTED);
        }
    }

    private String brandFolder() {
        return CLOUDINARY_ROOT_FOLDER + "/brands";
    }

    private String categoryFolder() {
        return CLOUDINARY_ROOT_FOLDER + "/categories";
    }

    private String productFolder(String productId) {
        return CLOUDINARY_ROOT_FOLDER + "/products/" + productId;
    }

    private String variantFolder(ProductVariant variant) {
        return productFolder(variant.getProduct().getId()) + "/" + variant.getId();
    }
}
