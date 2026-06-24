package com.eximplatform.logistics.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.logistics.domain.ShipmentStatus;
import com.eximplatform.logistics.dto.CreateShipmentRequest;
import com.eximplatform.logistics.dto.ShipmentResponse;
import com.eximplatform.logistics.dto.TrackingEventRequest;
import com.eximplatform.logistics.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<ShipmentResponse> create(@Valid @RequestBody CreateShipmentRequest request) {
        return ApiResponse.ok(shipmentService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShipmentResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(shipmentService.get(id));
    }

    /** List shipments; optionally filter by {@code orderId} or {@code status}. */
    @GetMapping
    public ApiResponse<PageResponse<ShipmentResponse>> list(
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) ShipmentStatus status,
            Pageable pageable) {
        return ApiResponse.ok(shipmentService.list(orderId, status, pageable));
    }

    /** Record a tracking event (advances the shipment's status). */
    @PostMapping("/{id}/events")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<ShipmentResponse> addEvent(@PathVariable UUID id,
                                                  @Valid @RequestBody TrackingEventRequest request) {
        return ApiResponse.ok(shipmentService.addTrackingEvent(id, request));
    }
}
