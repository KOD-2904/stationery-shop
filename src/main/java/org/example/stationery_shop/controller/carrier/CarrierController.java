package org.example.stationery_shop.controller.carrier;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.stationery_shop.dto.request.carrier.CarrierRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.carrier.CarrierResponse;
import org.example.stationery_shop.dto.response.carrier.CarrierShipmentResponse;
import org.example.stationery_shop.service.CarrierService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carrier")
@PreAuthorize("hasAuthority('ROLE_CARRIER')")
public class CarrierController {
    private final CarrierService carrierService;

    @GetMapping("/profile")
    public ApiResponse<CarrierResponse> getMyProfile() {
        return ApiResponse.<CarrierResponse>builder()
                .code(200)
                .message("Success")
                .result(carrierService.getMyProfile())
                .build();
    }

    @PutMapping("/profile")
    public ApiResponse<CarrierResponse> updateMyProfile(@Valid @RequestBody CarrierRequest request) {
        return ApiResponse.<CarrierResponse>builder()
                .code(200)
                .message("Updated carrier profile successfully")
                .result(carrierService.updateMyProfile(request))
                .build();
    }

    @GetMapping("/shipments")
    public ApiResponse<List<CarrierShipmentResponse>> getMyShipments() {
        return ApiResponse.<List<CarrierShipmentResponse>>builder()
                .code(200)
                .message("Success")
                .result(carrierService.getMyShipments())
                .build();
    }

    @PatchMapping("/shipments/{assignmentId}/pickup")
    public ApiResponse<CarrierShipmentResponse> markPickedUp(@PathVariable String assignmentId) {
        return ApiResponse.<CarrierShipmentResponse>builder()
                .code(200)
                .message("Shipment picked up successfully")
                .result(carrierService.markPickedUp(assignmentId))
                .build();
    }

    @PatchMapping("/shipments/{assignmentId}/delivered")
    public ApiResponse<CarrierShipmentResponse> markDelivered(@PathVariable String assignmentId) {
        return ApiResponse.<CarrierShipmentResponse>builder()
                .code(200)
                .message("Shipment delivered successfully")
                .result(carrierService.markDelivered(assignmentId))
                .build();
    }

    @PatchMapping("/shipments/{assignmentId}/completed")
    public ApiResponse<CarrierShipmentResponse> markCompleted(@PathVariable String assignmentId) {
        return ApiResponse.<CarrierShipmentResponse>builder()
                .code(200)
                .message("Shipment completed successfully")
                .result(carrierService.markCompleted(assignmentId))
                .build();
    }
}
