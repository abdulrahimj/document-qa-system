const BASE_URL = `${import.meta.env.VITE_API_URL}/api/questions`

export async function askQuestion(question) {
  const res = await fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question }),
  })

  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw new Error(body?.error ?? 'Failed to get an answer')
  }

  return res.json()
}
