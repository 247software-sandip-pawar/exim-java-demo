package com.eximplatform.config;

import com.eximplatform.payments.domain.PaymentTerm;
import com.eximplatform.payments.repository.PaymentTermRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Seeds standard international-trade payment terms. MongoDB has no migrations; reference data is
 * inserted on startup if the collection is empty (idempotent).
 */
@Configuration
public class PaymentTermSeeder {

    private static final Logger log = LoggerFactory.getLogger(PaymentTermSeeder.class);

    private record Seed(String code, String description, int netDays) {}

    private static final List<Seed> SEED = List.of(
            new Seed("ADVANCE_100", "100% advance payment before shipment", 0),
            new Seed("ADVANCE_30", "30% advance, balance on shipment", 0),
            new Seed("NET_30", "Payment due 30 days after invoice", 30),
            new Seed("NET_60", "Payment due 60 days after invoice", 60),
            new Seed("CAD", "Cash against documents", 0),
            new Seed("LC_AT_SIGHT", "Letter of credit, payable at sight", 0)
    );

    @Bean
    CommandLineRunner seedPaymentTerms(PaymentTermRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            List<PaymentTerm> terms = SEED.stream().map(s -> {
                PaymentTerm t = new PaymentTerm();
                t.setCode(s.code());
                t.setDescription(s.description());
                t.setNetDays(s.netDays());
                return t;
            }).toList();
            repository.saveAll(terms);
            log.info("Seeded {} payment terms", terms.size());
        };
    }
}
