package com.groupten.documentqa.qa.dto;

// One document that contributed a chunk of context to an answer.
public record SourceReference(String documentId, String filename) {
}
