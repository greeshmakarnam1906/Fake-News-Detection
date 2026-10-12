package com.fakenews.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "analyses", indexes = {
        @Index(name = "idx_analyses_user_created", columnList = "user_id, created_at")
})
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 500)
    private String headline;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String text;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(nullable = false)
    private Double score;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Analysis() {
    }

    public Analysis(User user, String headline, String text, String sourceUrl,
                    String label, Double score, String modelVersion) {
        this.user = user;
        this.headline = headline;
        this.text = text;
        this.sourceUrl = sourceUrl;
        this.label = label;
        this.score = score;
        this.modelVersion = modelVersion;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
