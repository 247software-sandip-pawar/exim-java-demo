package com.eximplatform.logistics.repository;

import com.eximplatform.logistics.domain.Shipment;
import com.eximplatform.logistics.domain.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ShipmentRepository extends MongoRepository<Shipment, UUID> {
    Page<Shipment> findByOrderId(UUID orderId, Pageable pageable);
    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);
}
