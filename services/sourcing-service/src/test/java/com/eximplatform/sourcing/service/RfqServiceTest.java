package com.eximplatform.sourcing.service;

import com.eximplatform.common.client.CatalogClient;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.client.ProductMatch;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.sourcing.domain.Rfq;
import com.eximplatform.sourcing.domain.RfqStatus;
import com.eximplatform.sourcing.dto.RfqMatchesResponse;
import com.eximplatform.sourcing.dto.RfqRequest;
import com.eximplatform.sourcing.dto.RfqResponse;
import com.eximplatform.sourcing.repository.RfqRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RfqServiceTest {

    @Mock RfqRepository rfqRepository;
    @Mock IdentityClient identityClient;
    @Mock CatalogClient catalogClient;

    @InjectMocks RfqService rfqService;

    private RfqRequest request(UUID buyerCompanyId) {
        RfqRequest req = new RfqRequest();
        req.setBuyerCompanyId(buyerCompanyId);
        req.setTitle("Need 500kg roasted coffee");
        req.setHsCode("0901.21");
        req.setQuantity(500);
        req.setUnit("kg");
        return req;
    }

    @Test
    void create_persistsOpenRfqWhenBuyerValid() {
        UUID buyer = UUID.randomUUID();
        when(identityClient.companyExists(buyer)).thenReturn(true);
        when(rfqRepository.save(any(Rfq.class))).thenAnswer(inv -> inv.getArgument(0));

        RfqResponse response = rfqService.create(request(buyer));

        assertThat(response.getTitle()).isEqualTo("Need 500kg roasted coffee");
        assertThat(response.getStatus()).isEqualTo("OPEN");
    }

    @Test
    void create_failsWhenBuyerMissing() {
        UUID buyer = UUID.randomUUID();
        when(identityClient.companyExists(buyer)).thenReturn(false);

        assertThatThrownBy(() -> rfqService.create(request(buyer)))
                .isInstanceOf(NotFoundException.class);

        verify(rfqRepository, never()).save(any());
    }

    @Test
    void matches_fetchesCatalogProductsByHsCode() {
        UUID id = UUID.randomUUID();
        Rfq rfq = new Rfq();
        rfq.setId(id);
        rfq.setHsCode("0901.21");
        rfq.setStatus(RfqStatus.OPEN);
        when(rfqRepository.findById(id)).thenReturn(Optional.of(rfq));
        ProductMatch match = new ProductMatch(UUID.randomUUID(), UUID.randomUUID(),
                "Arabica", "0901.21", new BigDecimal("12.50"), "USD", "kg", 100);
        when(catalogClient.findProductsByHsCode("0901.21")).thenReturn(List.of(match));

        RfqMatchesResponse response = rfqService.matches(id);

        assertThat(response.hsCode()).isEqualTo("0901.21");
        assertThat(response.count()).isEqualTo(1);
        assertThat(response.matches().get(0).name()).isEqualTo("Arabica");
    }

    @Test
    void matches_throwsWhenRfqMissing() {
        UUID id = UUID.randomUUID();
        when(rfqRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rfqService.matches(id))
                .isInstanceOf(NotFoundException.class);
    }
}
