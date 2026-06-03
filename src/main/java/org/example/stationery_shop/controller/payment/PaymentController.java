package org.example.stationery_shop.controller.payment;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.service.PaymentCallbackService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentCallbackService paymentCallbackService;

    @GetMapping("/vnpay-callback")
    public ApiResponse<String> vnpayReturn(@RequestParam Map<String, String> params) {
        return ApiResponse.<String>builder()
                .code(200)
                .message("Success")
                .result(paymentCallbackService.handleVnPayCallback(params))
                .build();
    }

    @GetMapping("/vnpay-ipn")
    public Map<String, String> vnpayIpnGet(@RequestParam Map<String, String> params) {
        return handleIpn(params);
    }

    @PostMapping("/vnpay-ipn")
    public Map<String, String> vnpayIpnPost(@RequestParam Map<String, String> params) {
        return handleIpn(params);
    }

    private Map<String, String> handleIpn(Map<String, String> params) {
        try {
            paymentCallbackService.handleVnPayCallback(params);
            return vnpayIpnResponse("00", "Confirm Success");
        } catch (AppException exception) {
            return vnpayIpnResponse(toVnPayIpnCode(exception.getErrorCode()), exception.getErrorCode().getMessage());
        } catch (Exception exception) {
            return vnpayIpnResponse("99", "Unknown error");
        }
    }

    private String toVnPayIpnCode(ErrorCode errorCode) {
        if (errorCode == ErrorCode.INVALID_PAYMENT_SIGNATURE) {
            return "97";
        }
        if (errorCode == ErrorCode.ORDER_NOT_EXIST) {
            return "01";
        }
        if (errorCode == ErrorCode.PAYMENT_AMOUNT_MISMATCH) {
            return "04";
        }
        return "99";
    }

    private Map<String, String> vnpayIpnResponse(String code, String message) {
        return Map.of("RspCode", code, "Message", message);
    }
}
