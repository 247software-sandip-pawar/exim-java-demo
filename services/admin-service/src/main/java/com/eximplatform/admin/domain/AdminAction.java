package com.eximplatform.admin.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An audit-log entry for an action taken by a platform admin. {@code actor} is the admin's email
 * (from the JWT); {@code target} identifies what was acted on (e.g. a verification id or a screened
 * name).
 */
@Document(collection = "admin_actions")
public class AdminAction extends BaseEntity {

    private String actor;

    @Indexed
    private AdminActionType type;

    private String target;

    private String detail;

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public AdminActionType getType() { return type; }
    public void setType(AdminActionType type) { this.type = type; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
