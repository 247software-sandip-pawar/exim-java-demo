package com.eximplatform.orders.repository;

import com.eximplatform.orders.domain.Order;
import com.eximplatform.orders.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends MongoRepository<Order, UUID> {
    Optional<Order> findByQuoteId(UUID quoteId);
    Page<Order> findByBuyerCompanyId(UUID buyerCompanyId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
