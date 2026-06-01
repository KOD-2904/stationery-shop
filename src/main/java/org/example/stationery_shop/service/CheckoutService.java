package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.checkout.CheckoutRequest;
import org.example.stationery_shop.dto.request.checkout.ShippingFeeRequest;
import org.example.stationery_shop.dto.response.checkout.CheckoutResponse;
import org.example.stationery_shop.dto.response.checkout.ShippingFeeResponse;

public interface CheckoutService {
    ShippingFeeResponse calculateShippingFee(ShippingFeeRequest request);
    CheckoutResponse checkout(CheckoutRequest request, String clientIp);
}
