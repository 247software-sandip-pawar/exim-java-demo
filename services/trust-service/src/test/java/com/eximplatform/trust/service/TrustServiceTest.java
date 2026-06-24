package com.eximplatform.trust.service;

import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.trust.domain.Dispute;
import com.eximplatform.trust.domain.DisputeStatus;
import com.eximplatform.trust.domain.Rating;
import com.eximplatform.trust.dto.DisputeRequest;
import com.eximplatform.trust.dto.DisputeResponse;
import com.eximplatform.trust.dto.DisputeStatusRequest;
import com.eximplatform.trust.dto.RatingRequest;
import com.eximplatform.trust.dto.RatingResponse;
import com.eximplatform.trust.dto.RatingSummaryResponse;
import com.eximplatform.trust.repository.DisputeRepository;
import com.eximplatform.trust.repository.RatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class TrustServiceTest {

    @Mock RatingRepository ratingRepository;
    @Mock DisputeRepository disputeRepository;
    @Mock IdentityClient identityClient;

    @InjectMocks RatingService ratingService;
    @InjectMocks DisputeService disputeService;

    private Rating rating(int score, UUID rated) {
        Rating r = new Rating();
        r.setRatedCompanyId(rated);
        r.setScore(score);
        return r;
    }

    @Test
    void createRating_persistsWhenCompaniesValid() {
        UUID rater = UUID.randomUUID();
        UUID rated = UUID.randomUUID();
        when(identityClient.companyExists(rater)).thenReturn(true);
        when(identityClient.companyExists(rated)).thenReturn(true);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        RatingRequest req = new RatingRequest();
        req.setOrderId(UUID.randomUUID());
        req.setRaterCompanyId(rater);
        req.setRatedCompanyId(rated);
        req.setScore(5);
        req.setComment("Great supplier");

        RatingResponse response = ratingService.create(req);

        assertThat(response.score()).isEqualTo(5);
    }

    @Test
    void ratingSummary_averagesScores() {
        UUID rated = UUID.randomUUID();
        when(ratingRepository.findByRatedCompanyId(rated))
                .thenReturn(List.of(rating(5, rated), rating(3, rated), rating(4, rated)));

        RatingSummaryResponse summary = ratingService.summary(rated);

        assertThat(summary.count()).isEqualTo(3);
        assertThat(summary.averageScore()).isEqualTo(4.0);
    }

    @Test
    void createDispute_failsWhenCompanyMissing() {
        UUID raisedBy = UUID.randomUUID();
        when(identityClient.companyExists(raisedBy)).thenReturn(false);

        DisputeRequest req = new DisputeRequest();
        req.setOrderId(UUID.randomUUID());
        req.setRaisedByCompanyId(raisedBy);
        req.setAgainstCompanyId(UUID.randomUUID());
        req.setReason("Goods not as described");

        assertThatThrownBy(() -> disputeService.create(req))
                .isInstanceOf(NotFoundException.class);

        verify(disputeRepository, never()).save(any());
    }

    @Test
    void updateDisputeStatus_rejectsInvalidTransition() {
        UUID id = UUID.randomUUID();
        Dispute dispute = new Dispute();
        dispute.setId(id);
        dispute.setStatus(DisputeStatus.OPEN);
        when(disputeRepository.findById(id)).thenReturn(Optional.of(dispute));

        DisputeStatusRequest req = new DisputeStatusRequest();
        req.setStatus(DisputeStatus.RESOLVED); // OPEN cannot jump straight to RESOLVED

        assertThatThrownBy(() -> disputeService.updateStatus(id, req))
                .isInstanceOf(BusinessException.class);

        verify(disputeRepository, never()).save(any());
    }

    @Test
    void updateDisputeStatus_allowsOpenToUnderReview() {
        UUID id = UUID.randomUUID();
        Dispute dispute = new Dispute();
        dispute.setId(id);
        dispute.setStatus(DisputeStatus.OPEN);
        when(disputeRepository.findById(id)).thenReturn(Optional.of(dispute));
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(inv -> inv.getArgument(0));

        DisputeStatusRequest req = new DisputeStatusRequest();
        req.setStatus(DisputeStatus.UNDER_REVIEW);

        DisputeResponse response = disputeService.updateStatus(id, req);

        assertThat(response.getStatus()).isEqualTo("UNDER_REVIEW");
    }
}
