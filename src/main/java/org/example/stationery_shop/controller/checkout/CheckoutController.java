package org.example.stationery_shop.controller.checkout;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.checkout.CartCheckoutRequest;
import org.example.stationery_shop.dto.request.checkout.CartShippingFeeRequest;
import org.example.stationery_shop.dto.request.checkout.CheckoutRequest;
import org.example.stationery_shop.dto.request.checkout.ShippingFeeRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.checkout.CheckoutResponse;
import org.example.stationery_shop.dto.response.checkout.ShippingFeeResponse;
import org.example.stationery_shop.service.CheckoutService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/checkout")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_USER')")
public class CheckoutController {
    private final CheckoutService checkoutService;

    @PostMapping("/shipping-fee")
    public ApiResponse<ShippingFeeResponse> calculateShippingFee(@Valid @RequestBody ShippingFeeRequest request) {
        return ApiResponse.<ShippingFeeResponse>builder()
                .code(200)
                .message("Calculated shipping fee successfully")
                .result(checkoutService.calculateShippingFee(request))
                .build();
    }

    @PostMapping("/shipping-fee/from-cart")
    public ApiResponse<ShippingFeeResponse> calculateShippingFeeFromCart(@Valid @RequestBody CartShippingFeeRequest request) {
        return ApiResponse.<ShippingFeeResponse>builder()
                .code(200)
                .message("Calculated shipping fee successfully")
                .result(checkoutService.calculateShippingFeeFromCart(request))
                .build();
    }

    @PostMapping
    public ApiResponse<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request,
                                                  HttpServletRequest servletRequest) {
        return ApiResponse.<CheckoutResponse>builder()
                .code(200)
                .message("Created checkout successfully")
                .result(checkoutService.checkout(request, clientIp(servletRequest)))
                .build();
    }

    @PostMapping("/buy-now")
    public ApiResponse<CheckoutResponse> buyNow(@Valid @RequestBody CheckoutRequest request,
                                                HttpServletRequest servletRequest) {
        return ApiResponse.<CheckoutResponse>builder()
                .code(200)
                .message("Created checkout successfully")
                .result(checkoutService.checkout(request, clientIp(servletRequest)))
                .build();
    }

    @PostMapping("/from-cart")
    public ApiResponse<CheckoutResponse> checkoutFromCart(@Valid @RequestBody CartCheckoutRequest request,
                                                         HttpServletRequest servletRequest) {
        return ApiResponse.<CheckoutResponse>builder()
                .code(200)
                .message("Created checkout successfully")
                .result(checkoutService.checkoutFromCart(request, clientIp(servletRequest)))
                .build();
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Nếu là IPv6 localhost (0:0:0:0:0:0:0:1) -> chuyển thành IPv4
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }

        // Nếu có nhiều IP (X-Forwarded-For có thể trả về "client, proxy")
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
