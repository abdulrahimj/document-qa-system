package com.groupten.documentqa.document.dto;

import com.groupten.documentqa.document.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

// API-facing shape for a Document - deliberately excludes the file content,
// so listing documents never pulls the raw bytes over the wire.
public record DocumentResponse(
        UUID id,
        String filename,
        String contentType,
        long fileSizeBytes,
        DocumentStatus status,
        Instant uploadedAt
) {
}