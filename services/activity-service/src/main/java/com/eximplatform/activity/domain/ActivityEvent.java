package com.eximplatform.activity.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * One recorded action: who (actor email + role) did what (HTTP method + path) and the result
 * (status). The timestamp is {@code createdAt} from {@link BaseEntity}.
 */
@Document(collection = "activity_events")
public class ActivityEvent extends BaseEntity {

    @Indexed
    private String actor;

    private String role;
    private String method;
    private String path;
    private int status;

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
