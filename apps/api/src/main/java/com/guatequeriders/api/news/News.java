package com.guatequeriders.api.news;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "news")
public class News {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String title;
    @Column(nullable = false, length = 500)
    private String summary;
    @Column(nullable = false, columnDefinition = "text")
    private String body;
    @Column(nullable = false, length = 40)
    private String category;
    @Column(name = "media_json", nullable = false, columnDefinition = "text")
    private String mediaJson = "[]";
    private boolean published;
    private boolean featured;
    @Column(name = "publish_at")
    private OffsetDateTime publishAt;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getSummary() { return summary; }
    public void setSummary(String value) { summary = value; }
    public String getBody() { return body; }
    public void setBody(String value) { body = value; }
    public String getCategory() { return category; }
    public void setCategory(String value) { category = value; }
    public String getMediaJson() { return mediaJson; }
    public void setMediaJson(String value) { mediaJson = value; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean value) { published = value; }
    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean value) { featured = value; }
    public OffsetDateTime getPublishAt() { return publishAt; }
    public void setPublishAt(OffsetDateTime value) { publishAt = value; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    @PreUpdate public void updateTimestamp() { updatedAt = OffsetDateTime.now(); }
}
