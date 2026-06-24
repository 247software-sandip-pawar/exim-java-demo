package com.eximplatform.admin.service;

import com.eximplatform.admin.domain.AdminAction;
import com.eximplatform.admin.domain.AdminActionType;
import com.eximplatform.admin.domain.SanctionedEntity;
import com.eximplatform.admin.dto.SanctionScreenResponse;
import com.eximplatform.admin.repository.AdminActionRepository;
import com.eximplatform.admin.repository.SanctionedEntityRepository;
import com.eximplatform.common.client.VerificationClient;
import com.eximplatform.common.client.VerificationView;
import com.eximplatform.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class AdminServiceTest {

    @Mock AdminActionRepository actionRepository;
    @Mock SanctionedEntityRepository sanctionsRepository;
    @Mock VerificationClient verificationClient;

    @InjectMocks AdminService adminService;
    @InjectMocks SanctionsService sanctionsService;

    @Test
    void decideKyc_approvesViaClientAndAuditsTheAction() {
        UUID verificationId = UUID.randomUUID();
        VerificationView updated = new VerificationView(verificationId, UUID.randomUUID(),
                "IEC", "s3://doc", "APPROVED", "looks good");
        when(verificationClient.decide(verificationId, "APPROVED", "looks good"))
                .thenReturn(Optional.of(updated));

        VerificationView result = adminService.decideKyc(verificationId, true, "looks good", "admin@exim.com");

        assertThat(result.status()).isEqualTo("APPROVED");
        ArgumentCaptor<AdminAction> captor = ArgumentCaptor.forClass(AdminAction.class);
        verify(actionRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(AdminActionType.KYC_APPROVE);
        assertThat(captor.getValue().getActor()).isEqualTo("admin@exim.com");
    }

    @Test
    void decideKyc_failsWhenVerificationMissing() {
        UUID verificationId = UUID.randomUUID();
        when(verificationClient.decide(any(), any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.decideKyc(verificationId, false, null, "admin@exim.com"))
                .isInstanceOf(NotFoundException.class);

        verify(actionRepository, never()).save(any());
    }

    @Test
    void screen_returnsMatchesAndRecordsAction() {
        SanctionedEntity e = new SanctionedEntity();
        e.setName("Redline Trading Co");
        when(sanctionsRepository.findByNameContainingIgnoreCase("redline")).thenReturn(List.of(e));

        SanctionScreenResponse response = sanctionsService.screen("redline", "admin@exim.com");

        assertThat(response.hit()).isTrue();
        assertThat(response.count()).isEqualTo(1);
        verify(actionRepository).save(any(AdminAction.class));
    }

    @Test
    void screen_noMatchStillRecordsAction() {
        when(sanctionsRepository.findByNameContainingIgnoreCase("acme")).thenReturn(List.of());

        SanctionScreenResponse response = sanctionsService.screen("acme", "admin@exim.com");

        assertThat(response.hit()).isFalse();
        assertThat(response.count()).isZero();
        verify(actionRepository).save(any(AdminAction.class));
    }
}
