package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.address.AddressRequest;
import org.example.stationery_shop.dto.response.address.AddressResponse;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getMyAddresses();
    AddressResponse createAddress(AddressRequest request);
    AddressResponse updateAddress(String id, AddressRequest request);
    void deleteAddress(String id);
    AddressResponse setDefaultAddress(String id);
}
