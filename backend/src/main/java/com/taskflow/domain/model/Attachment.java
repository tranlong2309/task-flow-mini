package com.taskflow.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Attachment {
    private UUID id;
    private String fileName;
    private String fileUrl;
    private Long uploadedBy;
    private Instant createdAt;

    public Attachment() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public Attachment(String fileName, String fileUrl, Long uploadedBy) {
        this();
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.uploadedBy = uploadedBy;
    }

    // getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
