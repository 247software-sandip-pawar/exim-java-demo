package com.eximplatform.config;

import com.eximplatform.billing.domain.Plan;
import com.eximplatform.billing.repository.PlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the subscription plans (the platform's pricing tiers). MongoDB has no migrations; reference
 * data is inserted on startup if the collection is empty (idempotent). {@code -1} = unlimited.
 */
@Configuration
public class PlanSeeder {

    private static final Logger log = LoggerFactory.getLogger(PlanSeeder.class);

    private record Seed(String code, String name, String description, String price, int maxProducts, int maxRfqs) {}

    private static final List<Seed> SEED = List.of(
            new Seed("FREE", "Free", "Get started: list a few products and post a few RFQs", "0", 5, 5),
            new Seed("STARTER", "Starter", "For small exporters/importers", "49.00", 50, 50),
            new Seed("GROWTH", "Growth", "For growing trade desks", "199.00", 500, 500),
            new Seed("ENTERPRISE", "Enterprise", "Unlimited listings and sourcing", "999.00", -1, -1)
    );

    @Bean
    CommandLineRunner seedPlans(PlanRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            List<Plan> plans = SEED.stream().map(s -> {
                Plan p = new Plan();
                p.setCode(s.code());
                p.setName(s.name());
                p.setDescription(s.description());
                p.setPriceMonthly(new BigDecimal(s.price()));
                p.setCurrency("USD");
                p.setMaxProducts(s.maxProducts());
                p.setMaxRfqs(s.maxRfqs());
                p.setActive(true);
                return p;
            }).toList();
            repository.saveAll(plans);
            log.info("Seeded {} plans", plans.size());
        };
    }
}
