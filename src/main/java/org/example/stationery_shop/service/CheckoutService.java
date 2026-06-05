package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.checkout.CartCheckoutRequest;
import org.example.stationery_shop.dto.request.checkout.CartShippingFeeRequest;
import org.example.stationery_shop.dto.request.checkout.CheckoutRequest;
import org.example.stationery_shop.dto.request.checkout.ShippingFeeRequest;
import org.example.stationery_shop.dto.response.checkout.CheckoutResponse;
import org.example.stationery_shop.dto.response.checkout.ShippingFeeResponse;
import org.example.stationery_shop.dto.response.checkout.ShippingServiceResponse;

import java.util.List;

public interface CheckoutService {
    ShippingFeeResponse calculateShippingFee(ShippingFeeRequest request);
    ShippingFeeResponse calculateShippingFeeFromCart(CartShippingFeeRequest request);
    List<ShippingServiceResponse> getShippingServicesFromCart(CartShippingFeeRequest request);
    CheckoutResponse checkout(CheckoutRequest request, String clientIp);
    CheckoutResponse checkoutFromCart(CartCheckoutRequest request, String clientIp);
}
