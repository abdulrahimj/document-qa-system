package com.groupten.documentqa.qa;

import com.groupten.documentqa.document.DocumentRepository;
import com.groupten.documentqa.qa.dto.QuestionResponse;
import com.groupten.documentqa.qa.dto.SourceReference;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

// The "Generation" half of RAG: takes a question, retrieves the most similar
// document chunks from PGVector, then asks Claude to answer using only that
// retrieved context (grounding the answer in the uploaded documents instead
// of the model's general knowledge).
@Service
public class QuestionService {

    private static final int TOP_K = 5;

    private static final String SYSTEM_PROMPT = """
            You answer questions using ONLY the provided document excerpts as context.
            If the excerpts don't contain enough information to answer, say so plainly
            instead of guessing or using outside knowledge.
            """;

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final DocumentRepository documentRepository;

    public QuestionService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder,
            DocumentRepository documentRepository) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
        this.documentRepository = documentRepository;
    }

    public QuestionResponse answer(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty");
        }

        // Only the most recently uploaded document is "active" - older uploads
        // stay in PGVector but are excluded here so answers don't mix in
        // content from documents the UI no longer shows.
        var latestDocument = documentRepository.findFirstByOrderByUploadedAtDesc();
        if (latestDocument.isEmpty()) {
            return new QuestionResponse(
                    "I don't have any uploaded documents that relate to this question.",
                    List.of());
        }

        Filter.Expression scopeToLatestDocument = new FilterExpressionBuilder()
                .eq("documentId", latestDocument.get().getId().toString())
                .build();

        List<Document> matches = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(TOP_K)
                        .filterExpression(scopeToLatestDocument)
                        .build());

        if (matches.isEmpty()) {
            return new QuestionResponse(
                    "I don't have any uploaded documents that relate to this question.",
                    List.of());
        }

        String context = matches.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        String answer = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(u -> u.text("""
                                Context:
                                {context}

                                Question: {question}
                                """)
                        .param("context", context)
                        .param("question", question))
                .call()
                .content();

        return new QuestionResponse(answer, distinctSources(matches));
    }

    // Multiple chunks often come from the same document - collapse them into
    // one source entry each, keeping the order they were first retrieved in.
    private List<SourceReference> distinctSources(List<Document> matches) {
        return matches.stream()
                .collect(Collectors.toMap(
                        d -> (String) d.getMetadata().get("documentId"),
                        d -> new SourceReference(
                                (String) d.getMetadata().get("documentId"),
                                (String) d.getMetadata().get("filename")),
                        (first, duplicate) -> first,
                        LinkedHashMap::new))
                .values()
                .stream()
                .toList();
    }
}
