package com.eximplatform.trust.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.trust.domain.Rating;
import com.eximplatform.trust.dto.RatingRequest;
import com.eximplatform.trust.dto.RatingResponse;
import com.eximplatform.trust.dto.RatingSummaryResponse;
import com.eximplatform.trust.repository.RatingRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Company ratings. Rater and rated companies are validated against the identity service over REST.
 * The per-company summary (count + average) is computed from the stored ratings.
 */
@Service
@Transactional(transactionManager = "trustTransactionManager")
public class RatingService {

    private final RatingRepository ratingRepository;
    private final IdentityClient identityClient;

    public RatingService(RatingRepository ratingRepository, IdentityClient identityClient) {
        this.ratingRepository = ratingRepository;
        this.identityClient = identityClient;
    }

    public RatingResponse create(RatingRequest req) {
        if (!identityClient.companyExists(req.getRaterCompanyId())) {
            throw new NotFoundException("Rater company not found.");
        }
        if (!identityClient.companyExists(req.getRatedCompanyId())) {
            throw new NotFoundException("Rated company not found.");
        }
        Rating rating = new Rating();
        rating.setOrderId(req.getOrderId());
        rating.setRaterCompanyId(req.getRaterCompanyId());
        rating.setRatedCompanyId(req.getRatedCompanyId());
        rating.setScore(req.getScore());
        rating.setComment(req.getComment());
        return RatingResponse.from(ratingRepository.save(rating));
    }

    @Transactional(transactionManager = "trustTransactionManager", readOnly = true)
    public RatingResponse get(UUID id) {
        return RatingResponse.from(ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating not found.")));
    }

    @Transactional(transactionManager = "trustTransactionManager", readOnly = true)
    public PageResponse<RatingResponse> list(UUID ratedCompanyId, Pageable pageable) {
        var page = ratedCompanyId != null
                ? ratingRepository.findByRatedCompanyId(ratedCompanyId, pageable)
                : ratingRepository.findAll(pageable);
        return PageResponse.from(page, RatingResponse::from);
    }

    @Transactional(transactionManager = "trustTransactionManager", readOnly = true)
    public RatingSummaryResponse summary(UUID ratedCompanyId) {
        List<Rating> ratings = ratingRepository.findByRatedCompanyId(ratedCompanyId);
        double average = ratings.stream().mapToInt(Rating::getScore).average().orElse(0.0);
        return new RatingSummaryResponse(ratedCompanyId, ratings.size(), average);
    }
}
