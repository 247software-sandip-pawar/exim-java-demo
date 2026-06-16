package com.eximplatform.verification.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.verification.domain.Verification;
import com.eximplatform.verification.domain.VerificationStatus;
import com.eximplatform.verification.domain.VerificationType;
import com.eximplatform.verification.dto.VerificationRequest;
import com.eximplatform.verification.dto.VerificationResponse;
import com.eximplatform.verification.repository.VerificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock VerificationRepository verificationRepository;
    @Mock IdentityClient identityClient;

    @InjectMocks VerificationService verificationService;

    private VerificationRequest request() {
        VerificationRequest req = new VerificationRequest();
        req.setType(VerificationType.IEC);
        req.setFileUrl("https://files.example.com/iec.pdf");
        return req;
    }

    @Test
    void submit_persistsPendingVerification() {
        UUID companyId = UUID.randomUUID();
        when(identityClient.companyExists(companyId)).thenReturn(true);
        when(verificationRepository.save(any(Verification.class))).thenAnswer(inv -> inv.getArgument(0));

        VerificationResponse response = verificationService.submit(companyId, request());

        ArgumentCaptor<Verification> saved = ArgumentCaptor.forClass(Verification.class);
        verify(verificationRepository).save(saved.capture());
        assertThat(saved.getValue().getCompanyId()).isEqualTo(companyId);
        assertThat(saved.getValue().getStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(response.getType()).isEqualTo("IEC");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getFileUrl()).isEqualTo("https://files.example.com/iec.pdf");
    }

    @Test
    void submit_failsWhenCompanyMissing() {
        UUID companyId = UUID.randomUUID();
        when(identityClient.companyExists(companyId)).thenReturn(false);

        assertThatThrownBy(() -> verificationService.submit(companyId, request()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company not found");

        verify(verificationRepository, never()).save(any());
    }

    @Test
    void list_returnsCompanyVerifications() {
        UUID companyId = UUID.randomUUID();
        Verification v = new Verification();
        v.setCompanyId(companyId);
        v.setType(VerificationType.GST);
        v.setFileUrl("https://files.example.com/gst.pdf");
        v.setStatus(VerificationStatus.PENDING);
        Pageable pageable = PageRequest.of(0, 20);
        when(identityClient.companyExists(companyId)).thenReturn(true);
        when(verificationRepository.findByCompanyId(eq(companyId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(v), pageable, 1));

        PageResponse<VerificationResponse> page = verificationService.list(companyId, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getItems().get(0).getType()).isEqualTo("GST");
    }

    @Test
    void list_failsWhenCompanyMissing() {
        UUID companyId = UUID.randomUUID();
        when(identityClient.companyExists(companyId)).thenReturn(false);

        assertThatThrownBy(() -> verificationService.list(companyId, PageRequest.of(0, 20)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void get_returnsScopedVerification() {
        UUID companyId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Verification v = new Verification();
        v.setCompanyId(companyId);
        v.setType(VerificationType.BANK);
        v.setFileUrl("https://files.example.com/bank.pdf");
        v.setStatus(VerificationStatus.PENDING);
        when(verificationRepository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.of(v));

        assertThat(verificationService.get(companyId, id).getType()).isEqualTo("BANK");
    }

    @Test
    void get_throwsWhenMissing() {
        UUID companyId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        when(verificationRepository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificationService.get(companyId, id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Verification not found");
    }
}
