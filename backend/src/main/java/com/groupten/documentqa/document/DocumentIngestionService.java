package com.groupten.documentqa.document;

import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// Turns an uploaded Document into searchable vectors: extract its raw text
// (Tika handles PDF, DOCX, TXT, ...), split it into chunks small enough for
// accurate similarity search, then embed and store each chunk in PGVector.
// This is the "Retrieval" half of RAG - the question-answering feature reads
// these chunks back out via VectorStore.similaritySearch(...).
//
// Note: org.springframework.ai.document.Document (a chunk of text + metadata)
// and our own Document (the JPA entity/uploaded file) share a class name but
// are unrelated types - referenced here by fully-qualified name to avoid confusion.
@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingest(Document document) {
        TikaDocumentReader reader = new TikaDocumentReader(
                new ByteArrayResource(document.getContent()));

        List<org.springframework.ai.document.Document> extracted = reader.get();

        List<org.springframework.ai.document.Document> chunks =
                new TokenTextSplitter().apply(extracted);

        chunks.forEach(chunk -> chunk.getMetadata().putAll(Map.of(
                "documentId", document.getId().toString(),
                "filename", document.getFilename()
        )));

        vectorStore.add(chunks);
    }
}
