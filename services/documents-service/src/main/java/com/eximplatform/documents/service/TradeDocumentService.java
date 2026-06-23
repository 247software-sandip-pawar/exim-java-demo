package com.eximplatform.documents.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.OrderView;
import com.eximplatform.common.client.OrdersClient;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.documents.domain.DocumentStatus;
import com.eximplatform.documents.domain.DocumentType;
import com.eximplatform.documents.domain.TradeDocument;
import com.eximplatform.documents.dto.DocumentResponse;
import com.eximplatform.documents.dto.GenerateDocumentRequest;
import com.eximplatform.documents.repository.TradeDocumentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Generates trade documents for an order. The order is read from the orders service over REST
 * (never by touching its database); a document snapshots the order's amount/currency and records a
 * {@code fileUrl} placeholder. Real PDF rendering and object storage are deferred (Phase 4).
 */
@Service
@Transactional(transactionManager = "documentsTransactionManager")
public class TradeDocumentService {

    private final TradeDocumentRepository documentRepository;
    private final OrdersClient ordersClient;

    public TradeDocumentService(TradeDocumentRepository documentRepository, OrdersClient ordersClient) {
        this.documentRepository = documentRepository;
        this.ordersClient = ordersClient;
    }

    public DocumentResponse generate(GenerateDocumentRequest req) {
        OrderView order = ordersClient.getOrder(req.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found."));

        DocumentType type = req.getType();
        TradeDocument doc = new TradeDocument();
        doc.setOrderId(order.id());
        doc.setBuyerCompanyId(order.buyerCompanyId());
        doc.setSellerCompanyId(order.sellerCompanyId());
        doc.setType(type);
        doc.setTotalAmount(order.totalAmount());
        doc.setCurrency(order.currency());
        // Number and file location are derived from the document's app-assigned id (set in BaseEntity).
        String shortId = doc.getId().toString().substring(0, 8).toUpperCase();
        doc.setDocumentNumber(type.prefix() + "-" + shortId);
        doc.setFileUrl("/documents/" + doc.getId() + ".pdf");
        doc.setStatus(DocumentStatus.GENERATED);
        return DocumentResponse.from(documentRepository.save(doc));
    }

    @Transactional(transactionManager = "documentsTransactionManager", readOnly = true)
    public DocumentResponse get(UUID id) {
        return DocumentResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "documentsTransactionManager", readOnly = true)
    public PageResponse<DocumentResponse> list(UUID orderId, Pageable pageable) {
        var page = orderId != null
                ? documentRepository.findByOrderId(orderId, pageable)
                : documentRepository.findAll(pageable);
        return PageResponse.from(page, DocumentResponse::from);
    }

    private TradeDocument findOrThrow(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found."));
    }
}
