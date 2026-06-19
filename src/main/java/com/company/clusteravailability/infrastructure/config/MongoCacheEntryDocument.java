package com.company.clusteravailability.infrastructure.config;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class MongoCacheEntryDocument {

    @Id
    private String id;

    @Field("cache_name")
    private String cacheName;

    @Field("cache_key")
    private String cacheKey;

    @Field("payload")
    private String payload;

    @Field("type")
    private String type;

    @Field("expires_at")
    private Instant expiresAt;

    public MongoCacheEntryDocument() {
    }

    public MongoCacheEntryDocument(String id, String cacheName, String cacheKey, String payload, String type, Instant expiresAt) {
        this.id = id;
        this.cacheName = cacheName;
        this.cacheKey = cacheKey;
        this.payload = payload;
        this.type = type;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    public String getCacheName() {
        return cacheName;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public String getPayload() {
        return payload;
    }

    public String getType() {
        return type;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
