package com.eximplatform.config;

import com.eximplatform.logistics.domain.LogisticsPartner;
import com.eximplatform.logistics.domain.TransportMode;
import com.eximplatform.logistics.repository.LogisticsPartnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Seeds a small set of logistics partners so shipments can reference a carrier out of the box.
 * MongoDB has no migrations; reference data is inserted on startup if the collection is empty.
 */
@Configuration
public class LogisticsPartnerSeeder {

    private static final Logger log = LoggerFactory.getLogger(LogisticsPartnerSeeder.class);

    private record Seed(String code, String name, TransportMode mode) {}

    private static final List<Seed> SEED = List.of(
            new Seed("MAERSK", "Maersk Line", TransportMode.SEA),
            new Seed("MSC", "Mediterranean Shipping Co.", TransportMode.SEA),
            new Seed("DHL_AIR", "DHL Air Freight", TransportMode.AIR),
            new Seed("FEDEX", "FedEx Express", TransportMode.AIR),
            new Seed("DB_RAIL", "DB Cargo Rail", TransportMode.RAIL),
            new Seed("INLAND_ROAD", "Inland Road Haulage", TransportMode.ROAD)
    );

    @Bean
    CommandLineRunner seedLogisticsPartners(LogisticsPartnerRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            List<LogisticsPartner> partners = SEED.stream().map(s -> {
                LogisticsPartner p = new LogisticsPartner();
                p.setCode(s.code());
                p.setName(s.name());
                p.setMode(s.mode());
                p.setActive(true);
                return p;
            }).toList();
            repository.saveAll(partners);
            log.info("Seeded {} logistics partners", partners.size());
        };
    }
}
