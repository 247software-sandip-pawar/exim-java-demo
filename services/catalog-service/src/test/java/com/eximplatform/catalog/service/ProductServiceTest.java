package com.eximplatform.catalog.service;

import com.eximplatform.catalog.domain.Product;
import com.eximplatform.catalog.dto.ProductRequest;
import com.eximplatform.catalog.dto.ProductResponse;
import com.eximplatform.catalog.repository.HsCodeRepository;
import com.eximplatform.catalog.repository.ProductRepository;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock HsCodeRepository hsCodeRepository;
    @Mock IdentityClient identityClient;

    @InjectMocks ProductService productService;

    private ProductRequest request(UUID companyId) {
        ProductRequest req = new ProductRequest();
        req.setCompanyId(companyId);
        req.setName("Arabica Coffee Beans");
        req.setHsCode("0901.21");
        req.setUnitPrice(new BigDecimal("12.50"));
        req.setCurrency("USD");
        req.setUnit("kg");
        req.setMinOrderQty(100);
        return req;
    }

    @Test
    void create_persistsWhenCompanyAndHsCodeValid() {
        UUID companyId = UUID.randomUUID();
        when(identityClient.companyExists(companyId)).thenReturn(true);
        when(hsCodeRepository.existsByCode("0901.21")).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.create(request(companyId));

        assertThat(response.getName()).isEqualTo("Arabica Coffee Beans");
        assertThat(response.getHsCode()).isEqualTo("0901.21");
        assertThat(response.isActive()).isTrue(); // defaults to active when omitted
    }

    @Test
    void create_failsWhenCompanyMissing() {
        UUID companyId = UUID.randomUUID();
        when(identityClient.companyExists(companyId)).thenReturn(false);

        assertThatThrownBy(() -> productService.create(request(companyId)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_failsOnUnknownHsCode() {
        UUID companyId = UUID.randomUUID();
        when(identityClient.companyExists(companyId)).thenReturn(true);
        when(hsCodeRepository.existsByCode("0901.21")).thenReturn(false);

        assertThatThrownBy(() -> productService.create(request(companyId)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unknown HS code");

        verify(productRepository, never()).save(any());
    }

    @Test
    void update_appliesFieldsButKeepsOwner() {
        UUID id = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        Product existing = new Product();
        existing.setCompanyId(owner);
        existing.setHsCode("0901.21");
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(hsCodeRepository.existsByCode("1006.30")).thenReturn(true);

        ProductRequest req = request(UUID.randomUUID()); // a different companyId in the body
        req.setHsCode("1006.30");
        req.setName("Basmati Rice");

        ProductResponse response = productService.update(id, req);

        assertThat(response.getName()).isEqualTo("Basmati Rice");
        assertThat(response.getHsCode()).isEqualTo("1006.30");
        assertThat(response.getCompanyId()).isEqualTo(owner); // ownership unchanged
    }

    @Test
    void get_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        lenient().when(identityClient.companyExists(any())).thenReturn(true);
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.get(id))
                .isInstanceOf(NotFoundException.class);
    }
}
