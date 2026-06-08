package org.example.stationery_shop.entity.shipping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stationery_shop.entity.BaseEntity;
import org.example.stationery_shop.entity.auth.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "carrier")
public class Carrier extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "province_id")
    private Integer provinceId;

    @Column(name = "province_name", length = 255)
    private String provinceName;

    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "district_name", length = 255)
    private String districtName;

    @Column(name = "current_assigned_orders", nullable = false)
    private Integer currentAssignedOrders = 0;

    @Column(name = "max_active_orders", nullable = false)
    private Integer maxActiveOrders = 10;

    @Column(nullable = false)
    private boolean active = true;
}
