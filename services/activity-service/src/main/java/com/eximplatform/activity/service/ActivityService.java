package com.eximplatform.activity.service;

import com.eximplatform.activity.domain.ActivityEvent;
import com.eximplatform.activity.dto.ActivityEventRequest;
import com.eximplatform.activity.dto.ActivityEventResponse;
import com.eximplatform.activity.repository.ActivityEventRepository;
import com.eximplatform.common.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ActivityService {

    private final ActivityEventRepository repository;

    public ActivityService(ActivityEventRepository repository) {
        this.repository = repository;
    }

    /** Records one action. Actor/role are taken from the authenticated caller, not the request body. */
    public ActivityEventResponse record(String actor, String role, ActivityEventRequest req) {
        ActivityEvent e = new ActivityEvent();
        e.setActor(actor);
        e.setRole(role);
        e.setMethod(req.getMethod());
        e.setPath(req.getPath());
        e.setStatus(req.getStatus());
        return ActivityEventResponse.from(repository.save(e));
    }

    /** Lists activity, optionally filtered by actor (substring, case-insensitive). */
    public PageResponse<ActivityEventResponse> list(String actor, Pageable pageable) {
        var page = StringUtils.hasText(actor)
                ? repository.findByActorContainingIgnoreCase(actor, pageable)
                : repository.findAll(pageable);
        return PageResponse.from(page, ActivityEventResponse::from);
    }
}
