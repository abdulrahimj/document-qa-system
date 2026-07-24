import { useState } from 'react'
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

  const [question, setQuestion] = useState('')
  const [asking, setAsking] = useState(false)
  const [questionError, setQuestionError] = useState(null)
  const [result, setResult] = useState(null)

  async function handleUpload(event) {
    event.preventDefault()
    if (!selectedFile) return

    setUploading(true)
    setUploadError(null)
    try {
      const uploaded = await uploadDocument(selectedFile)
      setDocuments([uploaded])
      setSelectedFile(null)
      event.target.reset()
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

  return (
    <div id="app">
      <header>
        <h1>Document Q&amp;A</h1>
        <p>Upload documents, then ask questions about them.</p>
      </header>

      <section id="upload">
        <form onSubmit={handleUpload}>
          <input
            type="file"
            onChange={(e) => setSelectedFile(e.target.files[0] ?? null)}
          />
          <button type="submit" disabled={!selectedFile || uploading}>
            {uploading ? 'Uploading…' : 'Upload'}
          </button>
        </form>
        {uploadError && <p className="error">{uploadError}</p>}
      </section>

      <section id="documents">
        <h2>Documents</h2>
        {documents.length === 0 ? (
          <p className="empty">No documents uploaded yet.</p>
        ) : (
          <ul>
            {documents.map((doc) => (
              <li key={doc.id}>
                <span className="filename">{doc.filename}</span>
                <span className="status">{doc.status}</span>
                <span className="meta">{formatBytes(doc.fileSizeBytes)}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section id="ask">
        <h2>Ask a question</h2>
        <form onSubmit={handleAsk}>
          <input
            type="text"
            placeholder="What would you like to know?"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
          />
          <button type="submit" disabled={!question.trim() || asking}>
            {asking ? 'Thinking…' : 'Ask'}
          </button>
        </form>
        {questionError && <p className="error">{questionError}</p>}

        {result && (
          <div className="answer">
            <p className="answer-text">{result.answer}</p>
            {result.sources.length > 0 && (
              <ul className="sources">
                {result.sources.map((source) => (
                  <li key={source.documentId}>{source.filename}</li>
                ))}
              </ul>
            )}
          </div>
        )}
      </section>

      <footer>
        <p>Abdulrahim Jalloh &amp; Fatmata Sesay</p>
      </footer>
    </div>
  )
}

export default App
