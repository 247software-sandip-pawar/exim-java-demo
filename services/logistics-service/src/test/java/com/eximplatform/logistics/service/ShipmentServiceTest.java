package com.eximplatform.logistics.service;

import com.eximplatform.common.client.OrderView;
import com.eximplatform.common.client.OrdersClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.logistics.domain.Shipment;
import com.eximplatform.logistics.domain.ShipmentStatus;
import com.eximplatform.logistics.domain.TransportMode;
import com.eximplatform.logistics.dto.CreateShipmentRequest;
import com.eximplatform.logistics.dto.ShipmentResponse;
import com.eximplatform.logistics.dto.TrackingEventRequest;
import com.eximplatform.logistics.repository.LogisticsPartnerRepository;
import com.eximplatform.logistics.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock ShipmentRepository shipmentRepository;
    @Mock LogisticsPartnerRepository partnerRepository;
    @Mock OrdersClient ordersClient;

    @InjectMocks ShipmentService shipmentService;

    private OrderView order() {
        return new OrderView(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), new BigDecimal("6250.00"), "USD", "CONFIRMED");
    }

    private CreateShipmentRequest request(UUID orderId, UUID partnerId) {
        CreateShipmentRequest req = new CreateShipmentRequest();
        req.setOrderId(orderId);
        req.setPartnerId(partnerId);
        req.setMode(TransportMode.SEA);
        req.setOrigin("Santos, BR");
        req.setDestination("Hamburg, DE");
        req.setIncoterm("FOB");
        return req;
    }

    @Test
    void create_booksShipmentFromOrderWithInitialEvent() {
        UUID orderId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        when(partnerRepository.existsById(partnerId)).thenReturn(true);
        when(ordersClient.getOrder(orderId)).thenReturn(Optional.of(order()));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        ShipmentResponse response = shipmentService.create(request(orderId, partnerId));

        assertThat(response.getStatus()).isEqualTo("CREATED");
        assertThat(response.getTrackingNumber()).startsWith("SHP-");
        assertThat(response.getEvents()).hasSize(1);
        assertThat(response.getEvents().get(0).status()).isEqualTo("CREATED");
    }

    @Test
    void create_failsWhenPartnerMissing() {
        UUID partnerId = UUID.randomUUID();
        when(partnerRepository.existsById(partnerId)).thenReturn(false);

        assertThatThrownBy(() -> shipmentService.create(request(UUID.randomUUID(), partnerId)))
                .isInstanceOf(NotFoundException.class);

        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void create_failsWhenOrderMissing() {
        UUID orderId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        when(partnerRepository.existsById(partnerId)).thenReturn(true);
        when(ordersClient.getOrder(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.create(request(orderId, partnerId)))
                .isInstanceOf(NotFoundException.class);

        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void addTrackingEvent_advancesStatusOnValidTransition() {
        UUID id = UUID.randomUUID();
        Shipment shipment = new Shipment();
        shipment.setId(id);
        shipment.setStatus(ShipmentStatus.CREATED);
        when(shipmentRepository.findById(id)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        TrackingEventRequest req = new TrackingEventRequest();
        req.setStatus(ShipmentStatus.BOOKED);
        req.setLocation("Santos, BR");

        ShipmentResponse response = shipmentService.addTrackingEvent(id, req);

        assertThat(response.getStatus()).isEqualTo("BOOKED");
        assertThat(response.getEvents()).hasSize(1);
    }

    @Test
    void addTrackingEvent_rejectsInvalidTransition() {
        UUID id = UUID.randomUUID();
        Shipment shipment = new Shipment();
        shipment.setId(id);
        shipment.setStatus(ShipmentStatus.CREATED);
        when(shipmentRepository.findById(id)).thenReturn(Optional.of(shipment));

        TrackingEventRequest req = new TrackingEventRequest();
        req.setStatus(ShipmentStatus.DELIVERED);

        assertThatThrownBy(() -> shipmentService.addTrackingEvent(id, req))
                .isInstanceOf(BusinessException.class);

        verify(shipmentRepository, never()).save(any());
    }
}
