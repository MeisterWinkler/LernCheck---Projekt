package com.example.quiz.model;

import java.time.LocalDateTime;

public class TemplateSummary {
    private long id;
    private String title;
    private LocalDateTime createdAt;

    public TemplateSummary() {
    }

    public TemplateSummary(long id, String title, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}