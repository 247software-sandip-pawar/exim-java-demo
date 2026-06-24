package com.eximplatform.billing.service;

import com.eximplatform.billing.domain.Plan;
import com.eximplatform.billing.domain.PaymentStatus;
import com.eximplatform.billing.domain.PlatformPayment;
import com.eximplatform.billing.domain.Subscription;
import com.eximplatform.billing.domain.SubscriptionStatus;
import com.eximplatform.billing.dto.SubscribeRequest;
import com.eximplatform.billing.dto.SubscriptionResponse;
import com.eximplatform.billing.repository.PlanRepository;
import com.eximplatform.billing.repository.PlatformPaymentRepository;
import com.eximplatform.billing.repository.SubscriptionRepository;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Subscription lifecycle. A company is validated against the identity service over REST, the plan
 * must exist, and a company may hold at most one ACTIVE subscription. Subscribing also opens a
 * PENDING platform payment for the first period. MongoDB has no dirty-checking, so updates save.
 */
@Service
@Transactional(transactionManager = "billingTransactionManager")
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PlatformPaymentRepository paymentRepository;
    private final IdentityClient identityClient;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               PlanRepository planRepository,
                               PlatformPaymentRepository paymentRepository,
                               IdentityClient identityClient) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.paymentRepository = paymentRepository;
        this.identityClient = identityClient;
    }

    public SubscriptionResponse subscribe(SubscribeRequest req) {
        if (!identityClient.companyExists(req.getCompanyId())) {
            throw new NotFoundException("Company not found.");
        }
        Plan plan = planRepository.findByCode(req.getPlanCode())
                .orElseThrow(() -> new NotFoundException("Plan not found: " + req.getPlanCode()));
        if (subscriptionRepository.existsByCompanyIdAndStatus(req.getCompanyId(), SubscriptionStatus.ACTIVE)) {
            throw new BusinessException("ALREADY_SUBSCRIBED",
                    "Company already has an active subscription; cancel it before subscribing again.");
        }

        Instant now = Instant.now();
        Subscription sub = new Subscription();
        sub.setCompanyId(req.getCompanyId());
        sub.setPlanCode(plan.getCode());
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setStartedAt(now);
        sub.setCurrentPeriodEnd(now.plus(30, ChronoUnit.DAYS));
        sub.setAutoRenew(req.isAutoRenew());
        Subscription saved = subscriptionRepository.save(sub);

        // Open the first period's platform payment (PENDING). Free plans are auto-paid (zero due).
        PlatformPayment payment = new PlatformPayment();
        payment.setSubscriptionId(saved.getId());
        payment.setCompanyId(saved.getCompanyId());
        payment.setAmount(plan.getPriceMonthly());
        payment.setCurrency(plan.getCurrency());
        payment.setStatus(plan.getPriceMonthly() != null && plan.getPriceMonthly().signum() == 0
                ? PaymentStatus.PAID : PaymentStatus.PENDING);
        paymentRepository.save(payment);

        return SubscriptionResponse.from(saved);
    }

    @Transactional(transactionManager = "billingTransactionManager", readOnly = true)
    public SubscriptionResponse get(UUID id) {
        return SubscriptionResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "billingTransactionManager", readOnly = true)
    public PageResponse<SubscriptionResponse> list(UUID companyId, SubscriptionStatus status, Pageable pageable) {
        var page = companyId != null
                ? subscriptionRepository.findByCompanyId(companyId, pageable)
                : status != null
                ? subscriptionRepository.findByStatus(status, pageable)
                : subscriptionRepository.findAll(pageable);
        return PageResponse.from(page, SubscriptionResponse::from);
    }

    public SubscriptionResponse cancel(UUID id) {
        Subscription sub = findOrThrow(id);
        if (sub.getStatus() == SubscriptionStatus.CANCELLED || sub.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new BusinessException("NOT_CANCELLABLE",
                    "Subscription is " + sub.getStatus() + " and cannot be cancelled.");
        }
        sub.setStatus(SubscriptionStatus.CANCELLED);
        sub.setAutoRenew(false);
        return SubscriptionResponse.from(subscriptionRepository.save(sub));
    }

    private Subscription findOrThrow(UUID id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subscription not found."));
    }
}
