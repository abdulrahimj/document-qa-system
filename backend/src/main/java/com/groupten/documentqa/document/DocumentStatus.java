package com.groupten.documentqa.document;

// Tracks a Document's progress through the ingestion pipeline (text
// extraction -> chunking -> embedding) so the UI can show what's happening.
public enum DocumentStatus {
    UPLOADED,
    PROCESSING,
    READY,
    FAILED
}
