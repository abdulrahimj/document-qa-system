package com.groupten.documentqa.document;

import com.groupten.documentqa.document.dto.DocumentResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentIngestionService documentIngestionService;

    public DocumentService(DocumentRepository documentRepository, DocumentIngestionService documentIngestionService) {
        this.documentRepository = documentRepository;
        this.documentIngestionService = documentIngestionService;
    }

    public DocumentResponse upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }

        // Same file already stored (by content, not just filename) - bump it
        // back to the top instead of storing and re-embedding a duplicate.
        Optional<Document> existing = documentRepository.findByContentHash(Document.computeContentHash(content));
        if (existing.isPresent()) {
            Document document = existing.get();
            document.refreshUploadTimestamp();
            return toResponse(documentRepository.save(document));
        }

        Document document = new Document(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                content
        );
        Document saved = documentRepository.save(document);

        // Runs synchronously so the upload response already reflects whether
        // ingestion succeeded - simplest option for a single-user local demo.
        saved.markProcessing();
        try {
            documentIngestionService.ingest(saved);
            saved.markReady();
        } catch (Exception e) {
            saved.markFailed();
        }

        return toResponse(documentRepository.save(saved));
    }

    public List<DocumentResponse> listAll() {
        return documentRepository.findAllByOrderByUploadedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFilename(),
                document.getContentType(),
                document.getFileSizeBytes(),
                document.getStatus(),
                document.getUploadedAt()
        );
    }
}