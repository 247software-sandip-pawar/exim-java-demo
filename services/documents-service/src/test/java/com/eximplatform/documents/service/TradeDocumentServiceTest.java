package com.eximplatform.documents.service;

import com.eximplatform.common.client.OrderView;
import com.eximplatform.common.client.OrdersClient;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.documents.domain.DocumentType;
import com.eximplatform.documents.domain.TradeDocument;
import com.eximplatform.documents.dto.DocumentResponse;
import com.eximplatform.documents.dto.GenerateDocumentRequest;
import com.eximplatform.documents.repository.TradeDocumentRepository;
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
class TradeDocumentServiceTest {

    @Mock TradeDocumentRepository documentRepository;
    @Mock OrdersClient ordersClient;

    @InjectMocks TradeDocumentService documentService;

    private OrderView order() {
        return new OrderView(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), new BigDecimal("6250.00"), "USD", "CONFIRMED");
    }

    private GenerateDocumentRequest request(UUID orderId, DocumentType type) {
        GenerateDocumentRequest req = new GenerateDocumentRequest();
        req.setOrderId(orderId);
        req.setType(type);
        return req;
    }

    @Test
    void generate_createsNumberedDocumentFromOrder() {
        UUID orderId = UUID.randomUUID();
        when(ordersClient.getOrder(orderId)).thenReturn(Optional.of(order()));
        when(documentRepository.save(any(TradeDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        DocumentResponse response = documentService.generate(request(orderId, DocumentType.PROFORMA_INVOICE));

        assertThat(response.getType()).isEqualTo("PROFORMA_INVOICE");
        assertThat(response.getStatus()).isEqualTo("GENERATED");
        assertThat(response.getDocumentNumber()).startsWith("PI-");
        assertThat(response.getFileUrl()).endsWith(".pdf");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("6250.00");
    }

    @Test
    void generate_failsWhenOrderMissing() {
        UUID orderId = UUID.randomUUID();
        when(ordersClient.getOrder(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.generate(request(orderId, DocumentType.COMMERCIAL_INVOICE)))
                .isInstanceOf(NotFoundException.class);

        verify(documentRepository, never()).save(any());
    }

    @Test
    void get_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(documentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.get(id))
                .isInstanceOf(NotFoundException.class);
    }
}
