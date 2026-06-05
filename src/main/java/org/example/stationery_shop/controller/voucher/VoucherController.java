package org.example.stationery_shop.controller.voucher;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.voucher.ValidateVoucherRequest;
import org.example.stationery_shop.dto.request.voucher.VoucherRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.voucher.VoucherResponse;
import org.example.stationery_shop.dto.response.voucher.VoucherValidationResponse;
import org.example.stationery_shop.service.VoucherService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vouchers")
public class VoucherController {
    private final VoucherService voucherService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ApiResponse<List<VoucherResponse>> getVouchers(
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ApiResponse.<List<VoucherResponse>>builder()
                .code(200)
                .message("Success")
                .result(voucherService.getVouchers(includeInactive))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ApiResponse<VoucherResponse> createVoucher(@Valid @RequestBody VoucherRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .code(200)
                .message("Created voucher successfully")
                .result(voucherService.createVoucher(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ApiResponse<VoucherResponse> updateVoucher(
            @PathVariable String id,
            @Valid @RequestBody VoucherRequest request
    ) {
        return ApiResponse.<VoucherResponse>builder()
                .code(200)
                .message("Updated voucher successfully")
                .result(voucherService.updateVoucher(id, request))
                .build();
    }

    @PostMapping("/validate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_USER')")
    public ApiResponse<VoucherValidationResponse> validateVoucher(
            @Valid @RequestBody ValidateVoucherRequest request
    ) {
        return ApiResponse.<VoucherValidationResponse>builder()
                .code(200)
                .message("Success")
                .result(voucherService.validateVoucher(request))
                .build();
    }
}
