package org.example.stationery_shop.controller.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.address.AddressRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.address.AddressResponse;
import org.example.stationery_shop.service.AddressService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_USER')")
public class AddressController {
    private final AddressService addressService;

    @GetMapping
    public ApiResponse<List<AddressResponse>> getMyAddresses() {
        return ApiResponse.<List<AddressResponse>>builder()
                .code(200)
                .message("Success")
                .result(addressService.getMyAddresses())
                .build();
    }

    @PostMapping
    public ApiResponse<AddressResponse> createAddress(@Valid @RequestBody AddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .code(200)
                .message("Created address successfully")
                .result(addressService.createAddress(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> updateAddress(@PathVariable String id, @Valid @RequestBody AddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .code(200)
                .message("Updated address successfully")
                .result(addressService.updateAddress(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable String id) {
        addressService.deleteAddress(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Deleted address successfully")
                .build();
    }

    @PatchMapping("/{id}/default")
    public ApiResponse<AddressResponse> setDefaultAddress(@PathVariable String id) {
        return ApiResponse.<AddressResponse>builder()
                .code(200)
                .message("Set default address successfully")
                .result(addressService.setDefaultAddress(id))
                .build();
    }
}
