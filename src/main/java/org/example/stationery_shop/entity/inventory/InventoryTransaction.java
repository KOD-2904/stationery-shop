package org.example.stationery_shop.entity.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.example.stationery_shop.enums.InventoryTransactionType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inventory_transaction")
public class InventoryTransaction extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryTransactionType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "available_before", nullable = false)
    private Integer availableBefore;

    @Column(name = "available_after", nullable = false)
    private Integer availableAfter;

    @Column(name = "locked_before", nullable = false)
    private Integer lockedBefore;

    @Column(name = "locked_after", nullable = false)
    private Integer lockedAfter;

    @Column(length = 500)
    private String note;
}
