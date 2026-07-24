const BASE_URL = `${import.meta.env.VITE_API_URL}/api/documents`

export async function uploadDocument(file) {
  const formData = new FormData()
  formData.append('file', file)

  const res = await fetch(BASE_URL, {
    method: 'POST',
    body: formData,
  })

  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw new Error(body?.error ?? 'Upload failed')
  }

  return res.json()
}
