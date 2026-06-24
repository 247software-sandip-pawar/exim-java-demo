package com.eximplatform.config;

import com.eximplatform.admin.domain.SanctionedEntity;
import com.eximplatform.admin.repository.SanctionedEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Seeds a tiny, FICTIONAL sanctions/denied-party list so screening can be demonstrated. MongoDB has
 * no migrations; reference data is inserted on startup if the collection is empty. A production
 * system would sync an official feed (OFAC/EU/UN) rather than seed static rows.
 */
@Configuration
public class SanctionsSeeder {

    private static final Logger log = LoggerFactory.getLogger(SanctionsSeeder.class);

    private record Seed(String name, String country, String programme, String reason) {}

    private static final List<Seed> SEED = List.of(
            new Seed("Redline Trading Co", "—", "DEMO", "Fictional sample entry for screening demos"),
            new Seed("Blackport Holdings", "—", "DEMO", "Fictional sample entry for screening demos"),
            new Seed("Northwind Munitions", "—", "DEMO", "Fictional sample entry for screening demos")
    );

    @Bean
    CommandLineRunner seedSanctions(SanctionedEntityRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            List<SanctionedEntity> rows = SEED.stream().map(s -> {
                SanctionedEntity e = new SanctionedEntity();
                e.setName(s.name());
                e.setCountry(s.country());
                e.setProgramme(s.programme());
                e.setReason(s.reason());
                return e;
            }).toList();
            repository.saveAll(rows);
            log.info("Seeded {} sanctioned entities (demo data)", rows.size());
        };
    }
}
