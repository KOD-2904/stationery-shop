package org.example.stationery_shop.dto.response.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GhnOrderInfoResponse {
    private String orderId;
    private String shipmentId;
    private String ghnOrderCode;
    private String localShippingStatus;
    private String ghnStatus;
    private String ghnStatusName;
    private String ghnAction;
    private String expectedDeliveryTime;
    private String estimatedFromTime;
    private String estimatedToTime;
    private String pickupTime;
    private String orderDate;
    private String updatedDate;
    private BigDecimal shippingFee;
    private BigDecimal ghnTotalFee;
    private BigDecimal codAmount;
    private String fromName;
    private String fromPhone;
    private String fromAddress;
    private String toName;
    private String toPhone;
    private String toAddress;
    private String toWardCode;
    private Integer toDistrictId;
    private String sortCode;
    private Integer currentWarehouseId;
    private Integer pickWarehouseId;
    private Integer deliverWarehouseId;
    private Integer nextWarehouseId;
    private String lastLogStatus;
    private String lastLogUpdatedDate;
    private String driverName;
    private String driverPhone;
    private String tripCode;
}
