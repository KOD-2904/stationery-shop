package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.order.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, String> {
}
