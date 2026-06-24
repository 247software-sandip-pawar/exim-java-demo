package com.eximplatform.logistics.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.OrderView;
import com.eximplatform.common.client.OrdersClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.logistics.domain.Shipment;
import com.eximplatform.logistics.domain.ShipmentStatus;
import com.eximplatform.logistics.domain.TrackingEvent;
import com.eximplatform.logistics.dto.CreateShipmentRequest;
import com.eximplatform.logistics.dto.ShipmentResponse;
import com.eximplatform.logistics.dto.TrackingEventRequest;
import com.eximplatform.logistics.repository.LogisticsPartnerRepository;
import com.eximplatform.logistics.repository.ShipmentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Books and tracks shipments for orders. The order is read from the orders service over REST (never
 * by touching its database); the logistics partner must be one of the seeded partners. Tracking
 * events drive a guarded status state machine. MongoDB has no dirty-checking, so updates call save.
 */
@Service
@Transactional(transactionManager = "logisticsTransactionManager")
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final LogisticsPartnerRepository partnerRepository;
    private final OrdersClient ordersClient;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           LogisticsPartnerRepository partnerRepository,
                           OrdersClient ordersClient) {
        this.shipmentRepository = shipmentRepository;
        this.partnerRepository = partnerRepository;
        this.ordersClient = ordersClient;
    }

    public ShipmentResponse create(CreateShipmentRequest req) {
        if (!partnerRepository.existsById(req.getPartnerId())) {
            throw new NotFoundException("Logistics partner not found.");
        }
        OrderView order = ordersClient.getOrder(req.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found."));

        Shipment shipment = new Shipment();
        shipment.setOrderId(order.id());
        shipment.setBuyerCompanyId(order.buyerCompanyId());
        shipment.setSellerCompanyId(order.sellerCompanyId());
        shipment.setPartnerId(req.getPartnerId());
        shipment.setMode(req.getMode());
        shipment.setOrigin(req.getOrigin());
        shipment.setDestination(req.getDestination());
        shipment.setIncoterm(req.getIncoterm());
        shipment.setEstimatedDelivery(req.getEstimatedDelivery());
        shipment.setStatus(ShipmentStatus.CREATED);
        // Tracking number derived from the app-assigned id (set in BaseEntity).
        shipment.setTrackingNumber("SHP-" + shipment.getId().toString().substring(0, 10).toUpperCase());
        shipment.getEvents().add(new TrackingEvent(ShipmentStatus.CREATED, req.getOrigin(),
                "Shipment created", Instant.now()));
        return ShipmentResponse.from(shipmentRepository.save(shipment));
    }

    @Transactional(transactionManager = "logisticsTransactionManager", readOnly = true)
    public ShipmentResponse get(UUID id) {
        return ShipmentResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "logisticsTransactionManager", readOnly = true)
    public PageResponse<ShipmentResponse> list(UUID orderId, ShipmentStatus status, Pageable pageable) {
        var page = orderId != null
                ? shipmentRepository.findByOrderId(orderId, pageable)
                : status != null
                ? shipmentRepository.findByStatus(status, pageable)
                : shipmentRepository.findAll(pageable);
        return PageResponse.from(page, ShipmentResponse::from);
    }

    /** Record a tracking event, advancing the shipment to the requested status if allowed. */
    public ShipmentResponse addTrackingEvent(UUID id, TrackingEventRequest req) {
        Shipment shipment = findOrThrow(id);
        ShipmentStatus target = req.getStatus();
        if (!shipment.getStatus().allowedNext().contains(target)) {
            throw new BusinessException("INVALID_SHIPMENT_TRANSITION",
                    "Cannot move shipment from " + shipment.getStatus() + " to " + target + ".");
        }
        shipment.getEvents().add(new TrackingEvent(target, req.getLocation(), req.getNote(), Instant.now()));
        shipment.setStatus(target);
        return ShipmentResponse.from(shipmentRepository.save(shipment));
    }

    private Shipment findOrThrow(UUID id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found."));
    }
}
