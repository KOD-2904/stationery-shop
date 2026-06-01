package org.example.stationery_shop.controller.payment;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.service.PaymentCallbackService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentCallbackService paymentCallbackService;

    @GetMapping("/vnpay-return")
    public ApiResponse<String> vnpayReturn(@RequestParam Map<String, String> params) {
        return ApiResponse.<String>builder()
                .code(200)
                .message("Success")
                .result(paymentCallbackService.handleVnPayCallback(params))
                .build();
    }

    @GetMapping("/vnpay-ipn")
    public ApiResponse<String> vnpayIpnGet(@RequestParam Map<String, String> params) {
        return handleIpn(params);
    }

    @PostMapping("/vnpay-ipn")
    public ApiResponse<String> vnpayIpnPost(@RequestParam Map<String, String> params) {
        return handleIpn(params);
    }

    private ApiResponse<String> handleIpn(Map<String, String> params) {
        return ApiResponse.<String>builder()
                .code(200)
                .message("Success")
                .result(paymentCallbackService.handleVnPayCallback(params))
                .build();
    }
}
