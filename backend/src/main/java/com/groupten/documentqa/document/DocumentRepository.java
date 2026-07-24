package com.groupten.documentqa.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findAllByOrderByUploadedAtDesc();

    Optional<Document> findFirstByOrderByUploadedAtDesc();

    Optional<Document> findByContentHash(String contentHash);
}