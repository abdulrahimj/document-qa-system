# Project: Document Q&A System (RAG-based)

## Stack
- Backend: Java 25, Spring Boot 4.1.x, Spring AI 2.0, Spring Security, PostgreSQL + PGVector
- Frontend: React (JavaScript, not TypeScript), Vite, ESLint, plain CSS
- Build tools: Maven (backend), npm (frontend)

## AI Model Architecture (important — hybrid setup)
This project uses TWO separate AI components, not one:
- **Chat/Answer generation**: Anthropic (Claude) via spring-ai-starter-model-anthropic,
  using ANTHROPIC_API_KEY. This is what answers the user's questions.
- **Embeddings** (turning document chunks into vectors for PGVector): local
  Transformers/ONNX via spring-ai-starter-model-transformers. Runs in-process in
  the JVM, no API key, no external service. Vector dimension is 384.
  Reason: Anthropic does not offer an embeddings API, so embeddings had to come
  from a separate source. Local Transformers was chosen over Ollama to avoid
  running/managing a second background application, especially for the live demo.

## Project structure
document-qa-system/
├── backend/     (Spring Boot project, opened as Maven module in IntelliJ)
└── frontend/    (Vite + React project, opened as npm module in IntelliJ)

Both live in the same IntelliJ window under one project root. When making changes
that touch both frontend and backend (e.g. wiring a new endpoint to the UI),
treat them as one coordinated change, not two separate tasks.

## Non-negotiable rules
- UI MUST be plain black and white only. No gradients, no colored accents, anywhere.
- No Python. Everything AI-related goes through Spring AI.
- Explain concepts briefly in code comments where non-obvious — I'm learning Spring AI for the first time.
- Use PostgreSQL + PGVector for the vector store (I already know PostgreSQL).
- Follow standard Spring Boot layering: Controller -> Service -> Repository. No business logic in controllers.
- Use DTOs for API requests/responses, never expose JPA entities directly.
- Use Java 25 features where they genuinely simplify code (records, pattern matching), but don't force them in just to show off — clarity first.
- Commit-sized changes: implement one feature at a time, don't dump the whole app in one shot.
- Ask me before adding new dependencies I haven't approved.
- Do NOT write project documentation until I explicitly ask for it. Documentation
    is the last step, after the full app is built and tested.

## Context
This is a final-year university AI project (CS4244, Applied AI) built solo by a
Spring Boot developer with no prior Spring AI or ML experience. Documentation and
a 10-minute live demo are required, so code must stay clean, explainable, and
easy to walk through live.