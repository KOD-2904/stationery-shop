package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.ProductVariantImageImportRequest;
import org.example.stationery_shop.dto.response.ProductVariantImageImportJobResponse;

public interface ProductVariantImageImportService {
    ProductVariantImageImportJobResponse startImport(ProductVariantImageImportRequest request);
    ProductVariantImageImportJobResponse getJob(String jobId);
}
