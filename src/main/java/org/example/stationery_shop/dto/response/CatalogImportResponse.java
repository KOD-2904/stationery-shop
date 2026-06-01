package org.example.stationery_shop.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CatalogImportResponse {
    private int totalRows;
    private int successRows;
    private int failedRows;
    private int createdBrands;
    private int reusedBrands;
    private int createdCategories;
    private int reusedCategories;
    private int createdProducts;
    private int reusedProducts;
    private int createdVariants;
    private List<CatalogImportRowResult> rows;

    @Getter
    @Builder
    public static class CatalogImportRowResult {
        private int rowNumber;
        private String productSlug;
        private String sku;
        private boolean success;
        private String message;
    }
}
