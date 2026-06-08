package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.carrier.CarrierRequest;
import org.example.stationery_shop.dto.response.carrier.CarrierResponse;
import org.example.stationery_shop.dto.response.carrier.CarrierShipmentResponse;

import java.util.List;

public interface CarrierService {
    List<CarrierResponse> getCarriers();
    CarrierResponse createCarrier(CarrierRequest request);
    CarrierResponse updateCarrier(String id, CarrierRequest request);
    CarrierResponse updateCarrierActive(String id, boolean active);
    CarrierResponse getMyProfile();
    CarrierResponse updateMyProfile(CarrierRequest request);
    List<CarrierShipmentResponse> getMyShipments();
    CarrierShipmentResponse markPickedUp(String assignmentId);
    CarrierShipmentResponse markDelivered(String assignmentId);
    CarrierShipmentResponse markCompleted(String assignmentId);
}
