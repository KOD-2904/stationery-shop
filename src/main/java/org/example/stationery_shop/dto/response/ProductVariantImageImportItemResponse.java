package org.example.stationery_shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.stationery_shop.enums.ImageImportItemStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantImageImportItemResponse {
    private String productVariantId;
    private String fileName;
    private String localFilePath;
    private Integer sortOrder;
    private boolean primaryImage;
    private String cloudinaryFolder;
    private String cloudinaryPublicId;
    private String imageUrl;
    private ImageImportItemStatus status;
    private String errorMessage;
}
