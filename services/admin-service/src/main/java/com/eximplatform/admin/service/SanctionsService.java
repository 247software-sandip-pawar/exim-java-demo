package com.eximplatform.admin.service;

import com.eximplatform.admin.domain.AdminAction;
import com.eximplatform.admin.domain.AdminActionType;
import com.eximplatform.admin.domain.SanctionedEntity;
import com.eximplatform.admin.dto.SanctionMatch;
import com.eximplatform.admin.dto.SanctionScreenResponse;
import com.eximplatform.admin.repository.AdminActionRepository;
import com.eximplatform.admin.repository.SanctionedEntityRepository;
import com.eximplatform.common.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sanctions / denied-party screening against the seeded list. Each screen is recorded in the admin
 * audit log. A production system would screen against an official, regularly-synced feed.
 */
@Service
@Transactional(transactionManager = "adminTransactionManager")
public class SanctionsService {

    private final SanctionedEntityRepository sanctionsRepository;
    private final AdminActionRepository actionRepository;

    public SanctionsService(SanctionedEntityRepository sanctionsRepository,
                            AdminActionRepository actionRepository) {
        this.sanctionsRepository = sanctionsRepository;
        this.actionRepository = actionRepository;
    }

    public SanctionScreenResponse screen(String name, String actor) {
        List<SanctionMatch> matches = sanctionsRepository.findByNameContainingIgnoreCase(name).stream()
                .map(SanctionMatch::from)
                .toList();

        AdminAction action = new AdminAction();
        action.setActor(actor);
        action.setType(AdminActionType.SANCTION_SCREEN);
        action.setTarget(name);
        action.setDetail(matches.isEmpty() ? "No match" : matches.size() + " match(es)");
        actionRepository.save(action);

        return SanctionScreenResponse.of(name, matches);
    }

    @Transactional(transactionManager = "adminTransactionManager", readOnly = true)
    public PageResponse<SanctionMatch> list(Pageable pageable) {
        return PageResponse.from(sanctionsRepository.findAll(pageable), SanctionMatch::from);
    }
}
