package org.example.stationery_shop.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductImageImportResponse {
    private int totalRows;
    private int successRows;
    private int failedRows;
    private int createdImages;
    private int skippedImages;
    private List<ProductImageImportRowResult> rows;

    @Getter
    @Builder
    public static class ProductImageImportRowResult {
        private int rowNumber;
        private String productId;
        private int createdImages;
        private int skippedImages;
        private boolean success;
        private String message;
    }
}
