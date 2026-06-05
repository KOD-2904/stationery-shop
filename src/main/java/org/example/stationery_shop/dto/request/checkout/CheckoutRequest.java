package org.example.stationery_shop.dto.request.checkout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.stationery_shop.enums.DeliveryMethod;
import org.example.stationery_shop.enums.PaymentMethod;

import java.util.List;

@Data
public class CheckoutRequest {
    @NotNull(message = "Delivery method khong duoc rong")
    private DeliveryMethod deliveryMethod;
    @NotNull(message = "Payment method khong duoc rong")
    private PaymentMethod paymentMethod;
    private String addressId;
    private String shippingFeeSnapshotId;
    private String voucherCode;
    private String note;

    @Valid
    @NotEmpty(message = "Checkout item khong duoc rong")
    private List<CheckoutItemRequest> items;
}
