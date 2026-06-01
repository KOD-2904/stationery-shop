package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.address.AddressRequest;
import org.example.stationery_shop.dto.response.address.AddressResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.entity.customer.Address;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.AddressRepository;
import org.example.stationery_shop.service.AddressService;
import org.example.stationery_shop.service.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final CurrentUserService currentUserService;

    @Override
    public List<AddressResponse> getMyAddresses() {
        User user = currentUserService.getCurrentUser();
        return addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        User user = currentUserService.getCurrentUser();
        boolean firstAddress = !addressRepository.existsByUserId(user.getId());
        boolean defaultAddress = firstAddress || Boolean.TRUE.equals(request.getDefaultAddress());
        Address address = Address.builder()
                .user(user)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .provinceId(request.getProvinceId())
                .provinceName(request.getProvinceName())
                .districtId(request.getDistrictId())
                .districtName(request.getDistrictName())
                .wardCode(request.getWardCode())
                .wardName(request.getWardName())
                .detailAddress(request.getDetailAddress())
                .defaultAddress(defaultAddress)
                .build();
        Address saved = addressRepository.save(address);
        if (defaultAddress) {
            addressRepository.clearDefaultAddress(user.getId(), saved.getId());
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(String id, AddressRequest request) {
        User user = currentUserService.getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXIST));
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvinceId(request.getProvinceId());
        address.setProvinceName(request.getProvinceName());
        address.setDistrictId(request.getDistrictId());
        address.setDistrictName(request.getDistrictName());
        address.setWardCode(request.getWardCode());
        address.setWardName(request.getWardName());
        address.setDetailAddress(request.getDetailAddress());
        if (Boolean.TRUE.equals(request.getDefaultAddress())) {
            address.setDefaultAddress(true);
            addressRepository.clearDefaultAddress(user.getId(), address.getId());
        }
        return toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(String id) {
        User user = currentUserService.getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXIST));
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(String id) {
        User user = currentUserService.getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXIST));
        address.setDefaultAddress(true);
        addressRepository.clearDefaultAddress(user.getId(), address.getId());
        return toResponse(addressRepository.save(address));
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .provinceId(address.getProvinceId())
                .provinceName(address.getProvinceName())
                .districtId(address.getDistrictId())
                .districtName(address.getDistrictName())
                .wardCode(address.getWardCode())
                .wardName(address.getWardName())
                .detailAddress(address.getDetailAddress())
                .defaultAddress(address.isDefaultAddress())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
