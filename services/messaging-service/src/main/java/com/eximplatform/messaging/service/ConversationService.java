package com.eximplatform.messaging.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.messaging.domain.Conversation;
import com.eximplatform.messaging.domain.ConversationStatus;
import com.eximplatform.messaging.domain.Message;
import com.eximplatform.messaging.dto.ConversationRequest;
import com.eximplatform.messaging.dto.ConversationResponse;
import com.eximplatform.messaging.dto.MessageRequest;
import com.eximplatform.messaging.dto.MessageResponse;
import com.eximplatform.messaging.repository.ConversationRepository;
import com.eximplatform.messaging.repository.MessageRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Conversations and their messages.
 *
 * <p>Participant company existence is validated against the identity service over REST (no shared
 * databases). A message can only be posted by a participant of the conversation, and only while the
 * conversation is {@link ConversationStatus#OPEN}.
 */
@Service
@Transactional(transactionManager = "messagingTransactionManager")
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final IdentityClient identityClient;

    public ConversationService(ConversationRepository conversationRepository,
                               MessageRepository messageRepository,
                               IdentityClient identityClient) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.identityClient = identityClient;
    }

    public ConversationResponse create(ConversationRequest req) {
        for (UUID companyId : req.getParticipantCompanyIds()) {
            if (!identityClient.companyExists(companyId)) {
                throw new NotFoundException("Participant company not found: " + companyId);
            }
        }
        Conversation conversation = new Conversation();
        conversation.setSubject(req.getSubject());
        conversation.setRfqId(req.getRfqId());
        conversation.setParticipantCompanyIds(req.getParticipantCompanyIds());
        conversation.setStatus(ConversationStatus.OPEN);
        return ConversationResponse.from(conversationRepository.save(conversation));
    }

    @Transactional(transactionManager = "messagingTransactionManager", readOnly = true)
    public ConversationResponse get(UUID id) {
        return ConversationResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "messagingTransactionManager", readOnly = true)
    public PageResponse<ConversationResponse> list(UUID participantCompanyId, Pageable pageable) {
        var page = participantCompanyId != null
                ? conversationRepository.findByParticipantCompanyIds(participantCompanyId, pageable)
                : conversationRepository.findAll(pageable);
        return PageResponse.from(page, ConversationResponse::from);
    }

    public MessageResponse postMessage(UUID conversationId, MessageRequest req) {
        Conversation conversation = findOrThrow(conversationId);
        if (conversation.getStatus() != ConversationStatus.OPEN) {
            throw new BusinessException("CONVERSATION_CLOSED",
                    "Cannot post to a " + conversation.getStatus() + " conversation.");
        }
        if (!conversation.getParticipantCompanyIds().contains(req.getSenderCompanyId())) {
            throw new BusinessException("NOT_A_PARTICIPANT",
                    "Sender is not a participant of this conversation.");
        }
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderCompanyId(req.getSenderCompanyId());
        message.setSenderUserId(req.getSenderUserId());
        message.setBody(req.getBody());
        message.setOffer(req.getOffer() != null ? req.getOffer().toEntity() : null);
        return MessageResponse.from(messageRepository.save(message));
    }

    @Transactional(transactionManager = "messagingTransactionManager", readOnly = true)
    public PageResponse<MessageResponse> listMessages(UUID conversationId, Pageable pageable) {
        findOrThrow(conversationId); // 404 if the conversation does not exist
        var page = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);
        return PageResponse.from(page, MessageResponse::from);
    }

    private Conversation findOrThrow(UUID id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Conversation not found."));
    }
}
