package com.eximplatform.identity.service;

import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.identity.domain.Company;
import com.eximplatform.identity.domain.CompanyType;
import com.eximplatform.identity.dto.CompanyRequest;
import com.eximplatform.identity.dto.CompanyResponse;
import com.eximplatform.identity.repository.CompanyRepository;
import com.eximplatform.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock CompanyRepository companyRepository;
    @Mock UserRepository userRepository;

    @InjectMocks CompanyService companyService;

    private CompanyRequest request(Boolean verified) {
        CompanyRequest req = new CompanyRequest();
        req.setName("Global Importers LLC");
        req.setType(CompanyType.IMPORTER);
        req.setCountry("US");
        req.setIecCode("IEC1234567");
        req.setGstin("27AAAAA0000A1Z5");
        req.setVerified(verified);
        return req;
    }

    @Test
    void create_defaultsVerifiedToFalseWhenOmitted() {
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

        CompanyResponse response = companyService.create(request(null));

        assertThat(response.getName()).isEqualTo("Global Importers LLC");
        assertThat(response.getType()).isEqualTo("IMPORTER");
        assertThat(response.isVerified()).isFalse();
    }

    @Test
    void create_honoursVerifiedWhenProvided() {
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

        CompanyResponse response = companyService.create(request(Boolean.TRUE));

        assertThat(response.isVerified()).isTrue();
    }

    @Test
    void get_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.get(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_keepsVerifiedWhenOmitted() {
        UUID id = UUID.randomUUID();
        Company existing = new Company();
        existing.setName("Old");
        existing.setType(CompanyType.EXPORTER);
        existing.setVerified(true);
        when(companyRepository.findById(id)).thenReturn(Optional.of(existing));

        CompanyResponse response = companyService.update(id, request(null));

        assertThat(response.getName()).isEqualTo("Global Importers LLC");
        assertThat(response.isVerified()).isTrue(); // unchanged because request omitted it
    }

    @Test
    void delete_blockedWhenUsersExist() {
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Optional.of(new Company()));
        when(userRepository.existsByCompanyId(id)).thenReturn(true);

        assertThatThrownBy(() -> companyService.delete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("still has users");

        verify(companyRepository, never()).delete(any());
    }

    @Test
    void delete_succeedsWhenNoUsers() {
        UUID id = UUID.randomUUID();
        Company company = new Company();
        when(companyRepository.findById(id)).thenReturn(Optional.of(company));
        when(userRepository.existsByCompanyId(id)).thenReturn(false);

        companyService.delete(id);

        verify(companyRepository).delete(company);
    }
}
