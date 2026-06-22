package com.eximplatform.config;

import com.eximplatform.catalog.domain.HsCode;
import com.eximplatform.catalog.repository.HsCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds a small set of HS codes so products and RFQ matching share a vocabulary. Replaces the
 * Flyway {@code INSERT} that the relational schema used; MongoDB has no migrations, so reference
 * data is inserted on startup if the collection is empty (idempotent).
 */
@Configuration
public class HsCodeSeeder {

    private static final Logger log = LoggerFactory.getLogger(HsCodeSeeder.class);

    private static final Map<String, String> SEED = new LinkedHashMap<>();
    static {
        SEED.put("0901.21", "Coffee, roasted, not decaffeinated");
        SEED.put("5208.52", "Woven cotton fabrics, printed, plain weave");
        SEED.put("7308.90", "Structures and parts of structures, of iron or steel");
        SEED.put("8471.30", "Portable digital automatic data processing machines");
        SEED.put("1006.30", "Semi-milled or wholly milled rice");
    }

    @Bean
    CommandLineRunner seedHsCodes(HsCodeRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            List<HsCode> codes = SEED.entrySet().stream().map(e -> {
                HsCode c = new HsCode();
                c.setCode(e.getKey());
                c.setDescription(e.getValue());
                return c;
            }).toList();
            repository.saveAll(codes);
            log.info("Seeded {} HS codes", codes.size());
        };
    }
}
