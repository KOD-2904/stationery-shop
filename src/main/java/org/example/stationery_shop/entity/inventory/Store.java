package org.example.stationery_shop.entity.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stationery_shop.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "store", uniqueConstraints = {
        @UniqueConstraint(name = "uk_store_code", columnNames = "code")
})
public class Store extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "province_id")
    private Integer provinceId;

    @Column(name = "province_name", length = 255)
    private String provinceName;

    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "district_name", length = 255)
    private String districtName;

    @Column(name = "ward_code", length = 50)
    private String wardCode;

    @Column(name = "ward_name", length = 255)
    private String wardName;

    @Column(name = "detail_address", length = 500)
    private String detailAddress;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;
}
