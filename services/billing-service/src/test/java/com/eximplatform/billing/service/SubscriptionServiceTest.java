package com.eximplatform.billing.service;

import com.eximplatform.billing.domain.Plan;
import com.eximplatform.billing.domain.PlatformPayment;
import com.eximplatform.billing.domain.Subscription;
import com.eximplatform.billing.domain.SubscriptionStatus;
import com.eximplatform.billing.dto.SubscribeRequest;
import com.eximplatform.billing.dto.SubscriptionResponse;
import com.eximplatform.billing.repository.PlanRepository;
import com.eximplatform.billing.repository.PlatformPaymentRepository;
import com.eximplatform.billing.repository.SubscriptionRepository;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
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
class SubscriptionServiceTest {

    @Mock SubscriptionRepository subscriptionRepository;
    @Mock PlanRepository planRepository;
    @Mock PlatformPaymentRepository paymentRepository;
    @Mock IdentityClient identityClient;

    @InjectMocks SubscriptionService subscriptionService;

    private Plan plan(String code, String price) {
        Plan p = new Plan();
        p.setCode(code);
        p.setPriceMonthly(new BigDecimal(price));
        p.setCurrency("USD");
        return p;
    }

    private SubscribeRequest request(UUID companyId, String planCode) {
        SubscribeRequest req = new SubscribeRequest();
        req.setCompanyId(companyId);
        req.setPlanCode(planCode);
        req.setAutoRenew(true);
        return req;
    }

    @Test
    void subscribe_createsActiveSubscriptionAndPendingPayment() {
        UUID company = UUID.randomUUID();
        when(identityClient.companyExists(company)).thenReturn(true);
        when(planRepository.findByCode("STARTER")).thenReturn(Optional.of(plan("STARTER", "49.00")));
        when(subscriptionRepository.existsByCompanyIdAndStatus(company, SubscriptionStatus.ACTIVE)).thenReturn(false);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionResponse response = subscriptionService.subscribe(request(company, "STARTER"));

        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getPlanCode()).isEqualTo("STARTER");
        verify(paymentRepository).save(any(PlatformPayment.class));
    }

    @Test
    void subscribe_rejectsSecondActiveSubscription() {
        UUID company = UUID.randomUUID();
        when(identityClient.companyExists(company)).thenReturn(true);
        when(planRepository.findByCode("GROWTH")).thenReturn(Optional.of(plan("GROWTH", "199.00")));
        when(subscriptionRepository.existsByCompanyIdAndStatus(company, SubscriptionStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> subscriptionService.subscribe(request(company, "GROWTH")))
                .isInstanceOf(BusinessException.class);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void subscribe_failsWhenPlanMissing() {
        UUID company = UUID.randomUUID();
        when(identityClient.companyExists(company)).thenReturn(true);
        when(planRepository.findByCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.subscribe(request(company, "NOPE")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void cancel_movesActiveToCancelled() {
        UUID id = UUID.randomUUID();
        Subscription sub = new Subscription();
        sub.setId(id);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionResponse response = subscriptionService.cancel(id);

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
        assertThat(response.isAutoRenew()).isFalse();
    }
}
