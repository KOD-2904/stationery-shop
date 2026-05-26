package org.example.stationery_shop.entity.catalog;

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
@Table(name = "brand", uniqueConstraints = {
        @UniqueConstraint(name = "uk_brand_slug", columnNames = "slug")
})
public class Brand extends BaseEntity {
    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String slug;

    @Column(length = 1000)
    private String description;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(nullable = false)
    private boolean active = true;
}
