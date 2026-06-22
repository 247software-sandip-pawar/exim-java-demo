package com.eximplatform.common.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all MongoDB documents: an application-assigned UUID id,
 * optimistic-locking version, and audit timestamps.
 *
 * <p>The id is generated up front (rather than by the store) so references can be wired before the
 * first save. The {@code @Version} field is a nullable {@link Long} on purpose: Spring Data MongoDB
 * uses {@code null} to mean "not yet persisted" (insert) and any non-null value to mean update.
 * A primitive {@code long} would default to {@code 0} and be misread as new on every save.
 *
 * <p>{@link CreatedDate}/{@link LastModifiedDate} require {@code @EnableMongoAuditing}, which each
 * service enables in its {@code *MongoConfig}.
 */
public abstract class BaseEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
