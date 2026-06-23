package com.eximplatform.messaging.service;

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
import com.eximplatform.messaging.dto.OfferDto;
import com.eximplatform.messaging.repository.ConversationRepository;
import com.eximplatform.messaging.repository.MessageRepository;
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
class ConversationServiceTest {

    @Mock ConversationRepository conversationRepository;
    @Mock MessageRepository messageRepository;
    @Mock IdentityClient identityClient;

    @InjectMocks ConversationService conversationService;

    private Conversation openConversation(UUID id, UUID... participants) {
        Conversation c = new Conversation();
        c.setId(id);
        c.setSubject("Coffee deal");
        c.setParticipantCompanyIds(List.of(participants));
        c.setStatus(ConversationStatus.OPEN);
        return c;
    }

    @Test
    void create_validatesParticipantsAndPersists() {
        UUID buyer = UUID.randomUUID();
        UUID seller = UUID.randomUUID();
        when(identityClient.companyExists(buyer)).thenReturn(true);
        when(identityClient.companyExists(seller)).thenReturn(true);
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));

        ConversationRequest req = new ConversationRequest();
        req.setSubject("Coffee deal");
        req.setParticipantCompanyIds(List.of(buyer, seller));

        ConversationResponse response = conversationService.create(req);

        assertThat(response.getStatus()).isEqualTo("OPEN");
        assertThat(response.getParticipantCompanyIds()).containsExactly(buyer, seller);
    }

    @Test
    void create_failsWhenParticipantMissing() {
        UUID buyer = UUID.randomUUID();
        when(identityClient.companyExists(buyer)).thenReturn(false);

        ConversationRequest req = new ConversationRequest();
        req.setSubject("Coffee deal");
        req.setParticipantCompanyIds(List.of(buyer));

        assertThatThrownBy(() -> conversationService.create(req))
                .isInstanceOf(NotFoundException.class);

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void postMessage_persistsMessageWithOfferFromParticipant() {
        UUID convId = UUID.randomUUID();
        UUID sender = UUID.randomUUID();
        when(conversationRepository.findById(convId)).thenReturn(Optional.of(openConversation(convId, sender)));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        MessageRequest req = new MessageRequest();
        req.setSenderCompanyId(sender);
        req.setBody("Can you do 11.00/kg?");
        req.setOffer(new OfferDto(UUID.randomUUID(), 500, "kg", new BigDecimal("11.00"), "USD", "FOB"));

        MessageResponse response = conversationService.postMessage(convId, req);

        assertThat(response.getBody()).isEqualTo("Can you do 11.00/kg?");
        assertThat(response.getOffer()).isNotNull();
        assertThat(response.getOffer().unitPrice()).isEqualByComparingTo("11.00");
    }

    @Test
    void postMessage_rejectsNonParticipant() {
        UUID convId = UUID.randomUUID();
        UUID participant = UUID.randomUUID();
        UUID outsider = UUID.randomUUID();
        when(conversationRepository.findById(convId)).thenReturn(Optional.of(openConversation(convId, participant)));

        MessageRequest req = new MessageRequest();
        req.setSenderCompanyId(outsider);
        req.setBody("Let me in");

        assertThatThrownBy(() -> conversationService.postMessage(convId, req))
                .isInstanceOf(BusinessException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void postMessage_failsWhenConversationMissing() {
        UUID convId = UUID.randomUUID();
        when(conversationRepository.findById(convId)).thenReturn(Optional.empty());

        MessageRequest req = new MessageRequest();
        req.setSenderCompanyId(UUID.randomUUID());
        req.setBody("Hello?");

        assertThatThrownBy(() -> conversationService.postMessage(convId, req))
                .isInstanceOf(NotFoundException.class);
    }
}
