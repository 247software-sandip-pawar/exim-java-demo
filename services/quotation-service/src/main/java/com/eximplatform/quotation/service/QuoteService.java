package com.eximplatform.quotation.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.quotation.domain.Quote;
import com.eximplatform.quotation.domain.QuoteStatus;
import com.eximplatform.quotation.dto.CounterQuoteRequest;
import com.eximplatform.quotation.dto.QuoteRequest;
import com.eximplatform.quotation.dto.QuoteResponse;
import com.eximplatform.quotation.repository.QuoteRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Quote lifecycle: a seller submits a quote, the buyer accepts, rejects, or counters it.
 *
 * <p>Buyer and seller company existence is validated against the identity service over REST (no
 * shared databases). State transitions are guarded: only a {@link QuoteStatus#SUBMITTED} quote can
 * be acted on, so accepting/rejecting/countering an already-resolved quote is a 422, not a silent
 * no-op. MongoDB has no dirty-checking, so every mutation is followed by an explicit {@code save}.
 */
@Service
@Transactional(transactionManager = "quotationTransactionManager")
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final IdentityClient identityClient;

    public QuoteService(QuoteRepository quoteRepository, IdentityClient identityClient) {
        this.quoteRepository = quoteRepository;
        this.identityClient = identityClient;
    }

    public QuoteResponse submit(QuoteRequest req) {
        if (!identityClient.companyExists(req.getSellerCompanyId())) {
            throw new NotFoundException("Seller company not found.");
        }
        if (!identityClient.companyExists(req.getBuyerCompanyId())) {
            throw new NotFoundException("Buyer company not found.");
        }
        Quote quote = new Quote();
        quote.setRfqId(req.getRfqId());
        quote.setSellerCompanyId(req.getSellerCompanyId());
        quote.setBuyerCompanyId(req.getBuyerCompanyId());
        quote.setProductId(req.getProductId());
        quote.setProductName(req.getProductName());
        quote.setQuantity(req.getQuantity());
        quote.setUnit(req.getUnit());
        quote.setUnitPrice(req.getUnitPrice());
        quote.setCurrency(req.getCurrency());
        quote.setIncoterm(req.getIncoterm());
        quote.setValidUntil(req.getValidUntil());
        quote.setNotes(req.getNotes());
        quote.setStatus(QuoteStatus.SUBMITTED);
        return QuoteResponse.from(quoteRepository.save(quote));
    }

    @Transactional(transactionManager = "quotationTransactionManager", readOnly = true)
    public QuoteResponse get(UUID id) {
        return QuoteResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "quotationTransactionManager", readOnly = true)
    public PageResponse<QuoteResponse> list(UUID rfqId, QuoteStatus status, Pageable pageable) {
        var page = switch (filter(rfqId, status)) {
            case BOTH -> quoteRepository.findByRfqIdAndStatus(rfqId, status, pageable);
            case RFQ -> quoteRepository.findByRfqId(rfqId, pageable);
            case STATUS -> quoteRepository.findByStatus(status, pageable);
            case NONE -> quoteRepository.findAll(pageable);
        };
        return PageResponse.from(page, QuoteResponse::from);
    }

    public QuoteResponse accept(UUID id) {
        Quote quote = requireSubmitted(id);
        quote.setStatus(QuoteStatus.ACCEPTED);
        return QuoteResponse.from(quoteRepository.save(quote));
    }

    public QuoteResponse reject(UUID id) {
        Quote quote = requireSubmitted(id);
        quote.setStatus(QuoteStatus.REJECTED);
        return QuoteResponse.from(quoteRepository.save(quote));
    }

    /**
     * Counter a submitted quote: the original moves to {@link QuoteStatus#COUNTERED} and a new
     * {@link QuoteStatus#SUBMITTED} quote is created with restated terms, linked via parentQuoteId.
     */
    public QuoteResponse counter(UUID id, CounterQuoteRequest req) {
        Quote original = requireSubmitted(id);
        original.setStatus(QuoteStatus.COUNTERED);
        quoteRepository.save(original);

        Quote counter = new Quote();
        counter.setRfqId(original.getRfqId());
        counter.setSellerCompanyId(original.getSellerCompanyId());
        counter.setBuyerCompanyId(original.getBuyerCompanyId());
        counter.setProductId(original.getProductId());
        counter.setProductName(original.getProductName());
        counter.setQuantity(req.getQuantity());
        counter.setUnit(req.getUnit() != null ? req.getUnit() : original.getUnit());
        counter.setUnitPrice(req.getUnitPrice());
        counter.setCurrency(req.getCurrency());
        counter.setIncoterm(req.getIncoterm() != null ? req.getIncoterm() : original.getIncoterm());
        counter.setValidUntil(req.getValidUntil());
        counter.setNotes(req.getNotes());
        counter.setStatus(QuoteStatus.SUBMITTED);
        counter.setParentQuoteId(original.getId());
        return QuoteResponse.from(quoteRepository.save(counter));
    }

    private Quote requireSubmitted(UUID id) {
        Quote quote = findOrThrow(id);
        if (quote.getStatus() != QuoteStatus.SUBMITTED) {
            throw new BusinessException("QUOTE_NOT_ACTIONABLE",
                    "Quote is " + quote.getStatus() + "; only a SUBMITTED quote can be acted on.");
        }
        return quote;
    }

    private Quote findOrThrow(UUID id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quote not found."));
    }

    private Filter filter(UUID rfqId, QuoteStatus status) {
        if (rfqId != null && status != null) return Filter.BOTH;
        if (rfqId != null) return Filter.RFQ;
        if (status != null) return Filter.STATUS;
        return Filter.NONE;
    }

    private enum Filter { BOTH, RFQ, STATUS, NONE }
}
