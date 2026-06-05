package org.example.stationery_shop.service.shipping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.response.checkout.ShippingServiceResponse;
import org.example.stationery_shop.entity.checkout.ShippingFeeSnapshot;
import org.example.stationery_shop.entity.customer.Address;
import org.example.stationery_shop.entity.inventory.Store;
import org.example.stationery_shop.entity.order.Order;
import org.example.stationery_shop.entity.order.OrderItem;
import org.example.stationery_shop.enums.PaymentMethod;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GhnClient {
    private static final String FEE_PATH = "/shiip/public-api/v2/shipping-order/fee";
    private static final String AVAILABLE_SERVICES_PATH = "/shiip/public-api/v2/shipping-order/available-services";
    private static final String CREATE_ORDER_PATH = "/shiip/public-api/v2/shipping-order/create";
    private static final String ORDER_DETAIL_PATH = "/shiip/public-api/v2/shipping-order/detail";
    private static final String PROVINCE_PATH = "/shiip/public-api/master-data/province";
    private static final String DISTRICT_PATH = "/shiip/public-api/master-data/district";
    private static final String WARD_PATH = "/shiip/public-api/master-data/ward";
    private static final int SHOP_PAYS_SHIPPING_FEE = 1;
    private static final String REQUIRED_NOTE_NO_VIEW = "KHONGCHOXEMHANG";
    private static final BigDecimal MOCK_SHIPPING_FEE = BigDecimal.valueOf(30000);

    private final GhnProperties properties;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    public GhnFeeResult calculateFee(Store store, Address address, Integer weight, Integer length, Integer width, Integer height,
                                     Integer serviceId, Integer serviceTypeId, BigDecimal insuranceValue) {
        if (properties.shouldMockOrder()) {
            return mockFeeResult(serviceId, serviceTypeId);
        }
        validateBasicConfig();
        validateStoreAddress(store);
        Integer effectiveServiceTypeId = resolveServiceTypeId(serviceId, serviceTypeId);
        Map<String, Object> request = Map.ofEntries(
                Map.entry("from_district_id", store.getDistrictId()),
                Map.entry("from_ward_code", store.getWardCode()),
                Map.entry("to_district_id", address.getDistrictId()),
                Map.entry("to_ward_code", address.getWardCode()),
                Map.entry("length", length),
                Map.entry("width", width),
                Map.entry("height", height),
                Map.entry("weight", weight),
                Map.entry("insurance_value", toGhnMoney(normalizeInsuranceValue(insuranceValue))),
                Map.entry("service_type_id", effectiveServiceTypeId),
                Map.entry("items", List.of(defaultItem(weight, length, width, height)))
        );
        try {
            JsonNode response = post(FEE_PATH, request);
            JsonNode data = response.path("data");
            return GhnFeeResult.builder()
                    .fee(readMoney(data, "total"))
                    .serviceId(readNullableInt(data, "service_id", serviceId))
                    .serviceTypeId(effectiveServiceTypeId)
                    .build();
        } catch (RestClientException exception) {
            throw new AppException(ErrorCode.GHN_CALCULATE_FEE_FAILED, exception.getMessage());
        }
    }

    public List<ShippingServiceResponse> getAvailableServices(Store store, Address address) {
        if (properties.shouldMockOrder()) {
            return List.of(mockShippingService());
        }
        validateBasicConfig();
        validateStoreAddress(store);
        Map<String, Object> request = Map.of(
                "shop_id", properties.getShopId(),
                "from_district", store.getDistrictId(),
                "to_district", address.getDistrictId()
        );
        try {
            JsonNode data = post(AVAILABLE_SERVICES_PATH, request).path("data");
            if (!data.isArray()) {
                return List.of();
            }
            List<ShippingServiceResponse> services = new java.util.ArrayList<>();
            for (JsonNode item : data) {
                services.add(ShippingServiceResponse.builder()
                        .serviceId(readNullableInt(item, "service_id", null))
                        .serviceTypeId(readNullableInt(item, "service_type_id", properties.getDefaultServiceTypeId()))
                        .name(readNullableText(item, "service_name"))
                        .shortName(readNullableText(item, "short_name"))
                        .build());
            }
            return services;
        } catch (RestClientException exception) {
            throw new AppException(ErrorCode.GHN_CALCULATE_FEE_FAILED, exception.getMessage());
        }
    }

    public GhnCreateOrderResult createOrder(Order order, ShippingFeeSnapshot snapshot, PaymentMethod paymentMethod) {
        if (properties.shouldMockOrder()) {
            return GhnCreateOrderResult.builder()
                    .orderCode("MOCK-GHN-" + order.getId())
                    .totalFee(snapshot.getShippingFee() == null ? MOCK_SHIPPING_FEE : snapshot.getShippingFee())
                    .build();
        }
        validateCreateOrderConfig();
        Address address = order.getAddress();
        if (address == null) {
            throw new AppException(ErrorCode.ADDRESS_REQUIRED);
        }
        BigDecimal codAmount = paymentMethod == PaymentMethod.COD ? order.getTotalAmount() : BigDecimal.ZERO;
        Store store = order.getStore();
        validateStoreAddress(store);
        Integer effectiveServiceTypeId = resolveServiceTypeId(snapshot.getServiceId(), snapshot.getServiceTypeId());
        Map<String, Object> request = Map.ofEntries(
                Map.entry("payment_type_id", SHOP_PAYS_SHIPPING_FEE),
                Map.entry("required_note", REQUIRED_NOTE_NO_VIEW),
                Map.entry("client_order_code", order.getId()),
                Map.entry("from_name", store.getName()),
                Map.entry("from_phone", store.getPhone()),
                Map.entry("from_address", store.getDetailAddress()),
                Map.entry("from_ward_code", store.getWardCode()),
                Map.entry("from_district_id", store.getDistrictId()),
                Map.entry("to_name", order.getReceiverName()),
                Map.entry("to_phone", order.getReceiverPhone()),
                Map.entry("to_address", address.getDetailAddress()),
                Map.entry("to_ward_code", address.getWardCode()),
                Map.entry("to_district_id", address.getDistrictId()),
                Map.entry("weight", snapshot.getWeight()),
                Map.entry("length", snapshot.getLength()),
                Map.entry("width", snapshot.getWidth()),
                Map.entry("height", snapshot.getHeight()),
                Map.entry("insurance_value", toGhnMoney(normalizeInsuranceValue(snapshot.getInsuranceValue()))),
                Map.entry("cod_amount", toGhnMoney(codAmount)),
                Map.entry("service_type_id", effectiveServiceTypeId),
                Map.entry("content", "Order " + order.getId()),
                Map.entry("note", order.getNote() == null ? "" : order.getNote()),
                Map.entry("items", toItems(order))
        );
        try {
            JsonNode data = post(CREATE_ORDER_PATH, request).path("data");
            return GhnCreateOrderResult.builder()
                    .orderCode(data.path("order_code").asText())
                    .totalFee(readMoney(data, "total_fee"))
                    .build();
        } catch (RestClientException exception) {
            throw new AppException(ErrorCode.GHN_CREATE_ORDER_FAILED, exception.getMessage());
        }
    }

    public GhnOrderInfoResult getOrderInfo(String orderCode) {
        if (properties.shouldMockOrder()) {
            if (!StringUtils.hasText(orderCode)) {
                throw new AppException(ErrorCode.GHN_ORDER_CODE_NOT_EXIST);
            }
            return mockOrderInfo(orderCode);
        }
        validateBasicConfig();
        if (!StringUtils.hasText(orderCode)) {
            throw new AppException(ErrorCode.GHN_ORDER_CODE_NOT_EXIST);
        }
        try {
            JsonNode data = post(ORDER_DETAIL_PATH, Map.of("order_code", orderCode)).path("data");
            JsonNode lastLog = firstArrayItem(data.path("log"));
            JsonNode leadtimeOrder = data.path("leadtime_order");
            return GhnOrderInfoResult.builder()
                    .orderCode(data.path("order_code").asText(orderCode))
                    .status(readNullableText(data, "status"))
                    .statusName(readNullableText(data, "status_name"))
                    .action(readNullableText(data, "action"))
                    .expectedDeliveryTime(readNullableText(data, "leadtime"))
                    .estimatedFromTime(readNullableText(leadtimeOrder, "from_estimate_date"))
                    .estimatedToTime(readNullableText(leadtimeOrder, "to_estimate_date"))
                    .pickupTime(readNullableText(data, "pickup_time"))
                    .orderDate(readNullableText(data, "order_date"))
                    .updatedDate(readNullableText(data, "updated_date"))
                    .totalFee(readMoney(data, "total_fee"))
                    .codAmount(readMoney(data, "cod_amount"))
                    .fromName(readNullableText(data, "from_name"))
                    .fromPhone(readNullableText(data, "from_phone"))
                    .fromAddress(readNullableText(data, "from_address"))
                    .toName(readNullableText(data, "to_name"))
                    .toPhone(readNullableText(data, "to_phone"))
                    .toAddress(readNullableText(data, "to_address"))
                    .toWardCode(readNullableText(data, "to_ward_code"))
                    .toDistrictId(readNullableInt(data, "to_district_id", null))
                    .sortCode(readNullableText(data, "sort_code"))
                    .currentWarehouseId(readNullableInt(data, "current_warehouse_id", null))
                    .pickWarehouseId(readNullableInt(data, "pick_warehouse_id", null))
                    .deliverWarehouseId(readNullableInt(data, "deliver_warehouse_id", null))
                    .nextWarehouseId(readNullableInt(data, "next_warehouse_id", null))
                    .lastLogStatus(readNullableText(lastLog, "status"))
                    .lastLogUpdatedDate(readNullableText(lastLog, "updated_date"))
                    .driverName(readNullableText(lastLog, "driver_name"))
                    .driverPhone(readNullableText(lastLog, "driver_phone"))
                    .tripCode(readNullableText(lastLog, "trip_code"))
                    .build();
        } catch (RestClientException exception) {
            throw new AppException(ErrorCode.GHN_GET_ORDER_FAILED, exception.getMessage());
        }
    }

    public JsonNode getProvinces() {
        if (properties.shouldMockMasterData()) {
            return objectMapper.valueToTree(List.of(Map.of("ProvinceID", 202, "ProvinceName", "Da Nang")));
        }
        validateBasicConfig();
        try {
            return get(PROVINCE_PATH, Map.of()).path("data");
        } catch (RestClientException exception) {
            throw new AppException(ErrorCode.GHN_GET_ORDER_FAILED, exception.getMessage());
        }
    }

    public JsonNode getDistricts(Integer provinceId) {
        if (properties.shouldMockMasterData()) {
            return objectMapper.valueToTree(List.of(Map.of("DistrictID", 1524, "DistrictName", "Hai Chau", "ProvinceID", provinceId)));
        }
        validateBasicConfig();
        try {
            return get(DISTRICT_PATH, Map.of("province_id", provinceId)).path("data");
        } catch (RestClientException exception) {
            throw new AppException(ErrorCode.GHN_GET_ORDER_FAILED, exception.getMessage());
        }
    }

    public JsonNode getWards(Integer districtId) {
        if (properties.shouldMockMasterData()) {
            return objectMapper.valueToTree(List.of(Map.of("WardCode", "40101", "WardName", "Thach Thang", "DistrictID", districtId)));
        }
        validateBasicConfig();
        try {
            return get(WARD_PATH, Map.of("district_id", districtId)).path("data");
        } catch (RestClientException exception) {
            throw new AppException(ErrorCode.GHN_GET_ORDER_FAILED, exception.getMessage());
        }
    }

    private JsonNode post(String path, Map<String, Object> request) {
        String responseBody = restClientBuilder.baseUrl(properties.getBaseUrl())
                .build()
                .post()
                .uri(path)
                .header("Token", properties.getToken())
                .header("ShopId", String.valueOf(properties.getShopId()))
                .body(request)
                .retrieve()
                .body(String.class);
        JsonNode response = readJson(responseBody);
        if (response == null || response.path("code").asInt() != 200) {
            String message = response == null ? "Empty GHN response" : response.path("message").asText();
            throw new RestClientException(message);
        }
        return response;
    }

    private JsonNode get(String path, Map<String, Object> params) {
        String responseBody = restClientBuilder.baseUrl(properties.getBaseUrl())
                .build()
                .get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    params.forEach(builder::queryParam);
                    return builder.build();
                })
                .header("Token", properties.getToken())
                .retrieve()
                .body(String.class);
        JsonNode response = readJson(responseBody);
        if (response == null || response.path("code").asInt() != 200) {
            String message = response == null ? "Empty GHN response" : response.path("message").asText();
            throw new RestClientException(message);
        }
        return response;
    }

    private JsonNode readJson(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return null;
        }
        try {
            return objectMapper.readTree(responseBody);
        } catch (Exception exception) {
            throw new RestClientException("Cannot parse GHN response", exception);
        }
    }

    private void validateBasicConfig() {
        if (properties.shouldMockOrder()) {
            return;
        }
        if (!properties.isEnabled()
                || !StringUtils.hasText(properties.getBaseUrl())
                || !StringUtils.hasText(properties.getToken())
                || properties.getShopId() == null) {
            throw new AppException(ErrorCode.GHN_NOT_CONFIGURED);
        }
    }

    private void validateCreateOrderConfig() {
        validateBasicConfig();
    }

    private void validateStoreAddress(Store store) {
        if (properties.shouldMockOrder()) {
            return;
        }
        if (store == null
                || store.getDistrictId() == null
                || !StringUtils.hasText(store.getWardCode())
                || !StringUtils.hasText(store.getDetailAddress())
                || !StringUtils.hasText(store.getPhone())
                || !StringUtils.hasText(store.getName())) {
            throw new AppException(ErrorCode.GHN_NOT_CONFIGURED);
        }
    }

    private Integer resolveServiceTypeId(Integer serviceId, Integer serviceTypeId) {
        if (serviceId != null && serviceTypeId != null) {
            return serviceTypeId;
        }
        // GHN service_type_id=2 is used as the temporary default until the frontend supports service selection.
        return serviceTypeId == null ? properties.getDefaultServiceTypeId() : serviceTypeId;
    }

    private GhnFeeResult mockFeeResult(Integer serviceId, Integer serviceTypeId) {
        return GhnFeeResult.builder()
                .fee(MOCK_SHIPPING_FEE)
                .serviceId(serviceId == null ? 53320 : serviceId)
                .serviceTypeId(resolveServiceTypeId(serviceId, serviceTypeId))
                .build();
    }

    private ShippingServiceResponse mockShippingService() {
        return ShippingServiceResponse.builder()
                .serviceId(53320)
                .serviceTypeId(properties.getDefaultServiceTypeId())
                .name("GHN Mock Standard")
                .shortName("MOCK")
                .build();
    }

    private GhnOrderInfoResult mockOrderInfo(String orderCode) {
        String now = Instant.now().toString();
        return GhnOrderInfoResult.builder()
                .orderCode(orderCode)
                .status("created")
                .statusName("Mock created")
                .action("mock")
                .expectedDeliveryTime(now)
                .estimatedFromTime(now)
                .estimatedToTime(now)
                .pickupTime(now)
                .orderDate(now)
                .updatedDate(now)
                .totalFee(MOCK_SHIPPING_FEE)
                .codAmount(BigDecimal.ZERO)
                .lastLogStatus("created")
                .lastLogUpdatedDate(now)
                .build();
    }

    private Map<String, Object> defaultItem(Integer weight, Integer length, Integer width, Integer height) {
        return Map.of(
                "name", "Jewelry",
                "quantity", 1,
                "weight", weight,
                "length", length,
                "width", width,
                "height", height
        );
    }

    private List<Map<String, Object>> toItems(Order order) {
        return order.getItems().stream()
                .map(this::toItem)
                .toList();
    }

    private Map<String, Object> toItem(OrderItem item) {
        return Map.of(
                "name", item.getProductName(),
                "code", item.getSku(),
                "quantity", item.getQuantity(),
                "price", toGhnMoney(item.getUnitPrice())
        );
    }

    private int toGhnMoney(BigDecimal value) {
        if (value == null) {
            return 0;
        }
        return value.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private BigDecimal normalizeInsuranceValue(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal maxInsuranceValue = properties.getMaxInsuranceValue();
        if (maxInsuranceValue == null || maxInsuranceValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return value.min(maxInsuranceValue);
    }

    private BigDecimal readMoney(JsonNode data, String field) {
        return BigDecimal.valueOf(data.path(field).asLong());
    }

    private Integer readNullableInt(JsonNode data, String field, Integer defaultValue) {
        JsonNode value = data.path(field);
        return value.isMissingNode() || value.isNull() ? defaultValue : value.asInt();
    }

    private String readNullableText(JsonNode data, String field) {
        JsonNode value = data.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private JsonNode firstArrayItem(JsonNode node) {
        return node.isArray() && !node.isEmpty() ? node.get(0) : objectMapper.createObjectNode();
    }
}
