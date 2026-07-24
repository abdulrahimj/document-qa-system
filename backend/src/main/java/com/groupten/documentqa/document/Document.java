package com.groupten.documentqa.document;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

// JPA entity - the source of truth in Postgres. Never returned directly from
// the controller; DocumentService maps it to DocumentResponse first.
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long fileSizeBytes;

    // No @Lob: that maps byte[] to Postgres's "large object" (oid) type, which
    // can only be read inside an explicit transaction - reading it here (e.g.
    // via open-in-view, in autocommit mode) throws "Large Objects may not be
    // used in auto-commit mode". Plain byte[] maps to bytea instead, which
    // has no such restriction. Note @Basic(fetch = LAZY) below only takes
    // effect with Hibernate bytecode enhancement (not configured here), so
    // this column is still fetched eagerly for now.
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] content;

    // SHA-256 fingerprint of `content`, used to recognize re-uploads of a file
    // that's already stored so we don't duplicate it (or its embeddings).
    @Column(nullable = false, unique = true)
    private String contentHash;

    @Column(nullable = false)
    private Instant uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    protected Document() {
        // required by JPA
    }

    public Document(String filename, String contentType, long fileSizeBytes, byte[] content) {
        this.filename = filename;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.content = content;
        this.contentHash = computeContentHash(content);
        this.uploadedAt = Instant.now();
        this.status = DocumentStatus.UPLOADED;
    }

    // Same file bytes always produce the same hash, regardless of filename -
    // this is how DocumentService recognizes "you've already uploaded this".
    public static String computeContentHash(byte[] content) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha256.digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public void refreshUploadTimestamp() {
        this.uploadedAt = Instant.now();
    }

    public void markProcessing() {
        this.status = DocumentStatus.PROCESSING;
    }

    public void markReady() {
        this.status = DocumentStatus.READY;
    }

    public void markFailed() {
        this.status = DocumentStatus.FAILED;
    }

    public UUID getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public byte[] getContent() {
        return content;
    }

    public String getContentHash() {
        return contentHash;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public DocumentStatus getStatus() {
        return status;
    }
}