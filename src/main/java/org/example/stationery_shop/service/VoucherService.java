package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.voucher.ValidateVoucherRequest;
import org.example.stationery_shop.dto.request.voucher.VoucherRequest;
import org.example.stationery_shop.dto.response.voucher.VoucherResponse;
import org.example.stationery_shop.dto.response.voucher.VoucherValidationResponse;

import java.math.BigDecimal;
import java.util.List;

public interface VoucherService {
    List<VoucherResponse> getVouchers(boolean includeInactive);
    VoucherResponse createVoucher(VoucherRequest request);
    VoucherResponse updateVoucher(String id, VoucherRequest request);
    VoucherValidationResponse validateVoucher(ValidateVoucherRequest request);
    VoucherValidationResponse validateVoucher(String code, BigDecimal subtotal, List<String> productIds);
    void recordUsage(String code, String orderId);
}
