import { useRef, useState } from 'react'
import { uploadDocument } from './api/documents'
import { askQuestion } from './api/questions'
import './App.css'

function formatBytes(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function App() {
  const [documents, setDocuments] = useState([])
  const [selectedFile, setSelectedFile] = useState(null)
  const [uploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState(null)
  const [dragging, setDragging] = useState(false)
  const fileInputRef = useRef(null)

  const [question, setQuestion] = useState('')
  const [asking, setAsking] = useState(false)
  const [questionError, setQuestionError] = useState(null)
  const [result, setResult] = useState(null)

  function pickFile(file) {
    setSelectedFile(file ?? null)
    setUploadError(null)
  }

  function clearFile() {
    pickFile(null)
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  function handleDrop(event) {
    event.preventDefault()
    setDragging(false)
    pickFile(event.dataTransfer.files[0])
  }

  async function handleUpload(event) {
    event.preventDefault()
    if (!selectedFile) return

    setUploading(true)
    setUploadError(null)
    try {
      const uploaded = await uploadDocument(selectedFile)
      setDocuments([uploaded])
      clearFile()
    } catch (e) {
      setUploadError(e.message)
    } finally {
      setUploading(false)
    }
  }

  async function handleAsk(event) {
    event.preventDefault()
    const trimmedQuestion = question.trim()
    if (!trimmedQuestion) return

    setAsking(true)
    setQuestionError(null)
    try {
      setResult(await askQuestion(trimmedQuestion))
      setQuestion('')
    } catch (e) {
      setQuestionError(e.message)
      setResult(null)
    } finally {
      setAsking(false)
    }
  }

  // Enter submits, Shift+Enter adds a newline - expected behaviour for a chat-like box
  function handleQuestionKeyDown(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      event.currentTarget.form.requestSubmit()
    }
  }

  return (
    <div id="app">
      <header>
        <p className="eyebrow">RAG · Retrieval augmented answers</p>
        <h1>Document Q&amp;A</h1>
        <p className="tagline">Upload a document, then ask questions about it.</p>
      </header>

      <main>
        <section id="upload" aria-labelledby="upload-heading">
          <h2 id="upload-heading">1 · Upload a document</h2>

          <form onSubmit={handleUpload}>
            <label
              className={`dropzone${dragging ? ' is-dragging' : ''}`}
              onDragOver={(e) => {
                e.preventDefault()
                setDragging(true)
              }}
              onDragLeave={() => setDragging(false)}
              onDrop={handleDrop}
            >
              <input
                ref={fileInputRef}
                type="file"
                className="visually-hidden"
                onChange={(e) => pickFile(e.target.files[0])}
              />
              <span className="dropzone-title">
                {dragging ? 'Drop the file here' : 'Choose a file or drag it here'}
              </span>
              <span className="dropzone-hint">PDF, TXT and other text documents</span>
            </label>

            {selectedFile && (
              <div className="file-chip">
                <span className="file-chip-name" title={selectedFile.name}>
                  {selectedFile.name}
                </span>
                <span className="file-chip-size">{formatBytes(selectedFile.size)}</span>
                <button
                  type="button"
                  className="ghost"
                  onClick={clearFile}
                  disabled={uploading}
                  aria-label={`Remove ${selectedFile.name}`}
                >
                  Remove
                </button>
              </div>
            )}

            <div className="actions">
              <button type="submit" disabled={!selectedFile || uploading}>
                {uploading ? 'Uploading…' : 'Upload'}
              </button>
              {uploading && <span className="dots" aria-hidden="true" />}
            </div>
          </form>

          {uploadError && (
            <p className="error" role="alert">
              {uploadError}
            </p>
          )}
        </section>

        <section id="documents" aria-labelledby="documents-heading">
          <h2 id="documents-heading">2 · Documents</h2>
          {documents.length === 0 ? (
            <p className="empty">No documents uploaded yet.</p>
          ) : (
            <ul>
              {documents.map((doc) => (
                <li key={doc.id}>
                  <span className="filename" title={doc.filename}>
                    {doc.filename}
                  </span>
                  <span className="meta">{formatBytes(doc.fileSizeBytes)}</span>
                  <span className="status">{doc.status}</span>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section id="ask" aria-labelledby="ask-heading">
          <h2 id="ask-heading">3 · Ask a question</h2>

          <form onSubmit={handleAsk}>
            <textarea
              rows={3}
              placeholder="What would you like to know?"
              aria-label="Your question"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              onKeyDown={handleQuestionKeyDown}
            />
            <div className="actions">
              <button type="submit" disabled={!question.trim() || asking}>
                {asking ? 'Thinking…' : 'Ask'}
              </button>
              <span className="hint">Enter to send · Shift + Enter for a new line</span>
            </div>
          </form>

          {questionError && (
            <p className="error" role="alert">
              {questionError}
            </p>
          )}

          <div aria-live="polite">
            {asking && (
              <p className="thinking">
                Searching your documents
                <span className="dots" aria-hidden="true" />
              </p>
            )}

            {!asking && result && (
              <article className="answer">
                <p className="answer-text">{result.answer}</p>
                {result.sources.length > 0 && (
                  <>
                    <p className="sources-label">Sources</p>
                    <ul className="sources">
                      {result.sources.map((source) => (
                        <li key={source.documentId}>{source.filename}</li>
                      ))}
                    </ul>
                  </>
                )}
              </article>
            )}
          </div>
        </section>
      </main>

      <footer>
        <p>Abdulrahim Jalloh &amp; Fatmata Sesay</p>
      </footer>
    </div>
  )
}

export default App
