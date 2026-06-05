package org.example.stationery_shop.service.shipping;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ghn")
public class GhnProperties {
    private boolean enabled;
    private boolean mockEnabled;
    private boolean masterDataMockEnabled;
    private boolean orderMockEnabled;
    private String baseUrl;
    private String token;
    private Integer shopId;
    private Integer fromDistrictId;
    private String fromWardCode;
    private String fromName;
    private String fromPhone;
    private String fromAddress;
    private Integer defaultServiceTypeId = 2;
    //private BigDecimal maxInsuranceValue = BigDecimal.valueOf(0);
    private BigDecimal maxInsuranceValue;
    private BigDecimal maxCodAmount;
    private BigDecimal maxFeeDifference = BigDecimal.ZERO;

    public boolean shouldMockMasterData() {
        return masterDataMockEnabled;
    }

    public boolean shouldMockOrder() {
        return orderMockEnabled || mockEnabled;
    }
}
