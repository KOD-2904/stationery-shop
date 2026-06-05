package org.example.stationery_shop.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stationery_shop.enums.UserStatus;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private long totalOrders;
    private long pendingPaymentOrders;
    private long processingOrders;
    private long completedOrders;
    private long totalProducts;
    private long activeProducts;
    private long totalVariants;
    private long activeVariants;
    private long totalBrands;
    private long activeBrands;
    private long totalCategories;
    private long activeCategories;
    private long totalUsers;
    private Map<UserStatus, Long> usersByStatus;
}
