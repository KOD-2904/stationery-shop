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
    private String baseUrl;
    private String token;
    private Integer shopId;
    private Integer fromDistrictId;
    private String fromWardCode;
    private BigDecimal maxFeeDifference = BigDecimal.ZERO;
}
