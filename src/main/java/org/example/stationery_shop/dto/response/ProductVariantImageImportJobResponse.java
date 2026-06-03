package org.example.stationery_shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.stationery_shop.enums.ImageImportJobStatus;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantImageImportJobResponse {
    private String jobId;
    private ImageImportJobStatus status;
    private String rootFolder;
    private int totalItems;
    private int pendingItems;
    private int successItems;
    private int failedItems;
    private int progressPercent;
    private Instant createdAt;
    private Instant startedAt;
    private Instant finishedAt;
    private String errorMessage;
    private List<ProductVariantImageImportItemResponse> items;
}
