package com.eximplatform.quotation.service;

import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.quotation.domain.Quote;
import com.eximplatform.quotation.domain.QuoteStatus;
import com.eximplatform.quotation.dto.CounterQuoteRequest;
import com.eximplatform.quotation.dto.QuoteRequest;
import com.eximplatform.quotation.dto.QuoteResponse;
import com.eximplatform.quotation.repository.QuoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class QuoteServiceTest {

    @Mock QuoteRepository quoteRepository;
    @Mock IdentityClient identityClient;

    @InjectMocks QuoteService quoteService;

    private QuoteRequest request(UUID seller, UUID buyer) {
        QuoteRequest req = new QuoteRequest();
        req.setRfqId(UUID.randomUUID());
        req.setSellerCompanyId(seller);
        req.setBuyerCompanyId(buyer);
        req.setProductId(UUID.randomUUID());
        req.setProductName("Arabica");
        req.setQuantity(500);
        req.setUnit("kg");
        req.setUnitPrice(new BigDecimal("12.50"));
        req.setCurrency("USD");
        req.setIncoterm("FOB");
        return req;
    }

    private Quote submittedQuote(UUID id) {
        Quote q = new Quote();
        q.setId(id);
        q.setRfqId(UUID.randomUUID());
        q.setSellerCompanyId(UUID.randomUUID());
        q.setBuyerCompanyId(UUID.randomUUID());
        q.setProductId(UUID.randomUUID());
        q.setQuantity(500);
        q.setUnit("kg");
        q.setUnitPrice(new BigDecimal("12.50"));
        q.setCurrency("USD");
        q.setStatus(QuoteStatus.SUBMITTED);
        return q;
    }

    @Test
    void submit_persistsSubmittedQuoteWhenCompaniesValid() {
        UUID seller = UUID.randomUUID();
        UUID buyer = UUID.randomUUID();
        when(identityClient.companyExists(seller)).thenReturn(true);
        when(identityClient.companyExists(buyer)).thenReturn(true);
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        QuoteResponse response = quoteService.submit(request(seller, buyer));

        assertThat(response.getStatus()).isEqualTo("SUBMITTED");
        assertThat(response.getUnitPrice()).isEqualByComparingTo("12.50");
    }

    @Test
    void submit_failsWhenSellerMissing() {
        UUID seller = UUID.randomUUID();
        UUID buyer = UUID.randomUUID();
        when(identityClient.companyExists(seller)).thenReturn(false);

        assertThatThrownBy(() -> quoteService.submit(request(seller, buyer)))
                .isInstanceOf(NotFoundException.class);

        verify(quoteRepository, never()).save(any());
    }

    @Test
    void accept_transitionsSubmittedToAccepted() {
        UUID id = UUID.randomUUID();
        when(quoteRepository.findById(id)).thenReturn(Optional.of(submittedQuote(id)));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        QuoteResponse response = quoteService.accept(id);

        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    void accept_failsWhenQuoteAlreadyResolved() {
        UUID id = UUID.randomUUID();
        Quote q = submittedQuote(id);
        q.setStatus(QuoteStatus.ACCEPTED);
        when(quoteRepository.findById(id)).thenReturn(Optional.of(q));

        assertThatThrownBy(() -> quoteService.accept(id))
                .isInstanceOf(BusinessException.class);

        verify(quoteRepository, never()).save(any());
    }

    @Test
    void counter_marksOriginalCounteredAndCreatesLinkedChild() {
        UUID id = UUID.randomUUID();
        when(quoteRepository.findById(id)).thenReturn(Optional.of(submittedQuote(id)));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        CounterQuoteRequest req = new CounterQuoteRequest();
        req.setQuantity(400);
        req.setUnitPrice(new BigDecimal("11.00"));
        req.setCurrency("USD");

        QuoteResponse child = quoteService.counter(id, req);

        ArgumentCaptor<Quote> saved = ArgumentCaptor.forClass(Quote.class);
        verify(quoteRepository, org.mockito.Mockito.times(2)).save(saved.capture());
        Quote original = saved.getAllValues().get(0);
        assertThat(original.getStatus()).isEqualTo(QuoteStatus.COUNTERED);
        assertThat(child.getStatus()).isEqualTo("SUBMITTED");
        assertThat(child.getParentQuoteId()).isEqualTo(id);
        assertThat(child.getUnitPrice()).isEqualByComparingTo("11.00");
    }

    @Test
    void get_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(quoteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.get(id))
                .isInstanceOf(NotFoundException.class);
    }
}
