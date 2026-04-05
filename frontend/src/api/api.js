const API_BASE = '/auth'
const TASKS_BASE = '/tasks'

function getHeaders(token) {
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`
  return headers
}

// Auth
export async function login(username, password) {
  const res = await fetch(`${API_BASE}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.message || 'Login failed')
  return data
}

export async function register(username, password) {
  const res = await fetch(`${API_BASE}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.message || 'Registration failed')
  return data
}

// Tasks
export async function getTasks(token, { status, page = 0, size = 10 } = {}) {
  const params = new URLSearchParams({ page, size })
  if (status) params.set('status', status)

  const res = await fetch(`${TASKS_BASE}?${params}`, {
    headers: getHeaders(token)
  })
  if (res.status === 401) throw new Error('Unauthorized')
  const data = await res.json()
  if (!res.ok) throw new Error(data.message || 'Failed to fetch tasks')
  return data
}

export async function getTaskById(token, id) {
  const res = await fetch(`${TASKS_BASE}/${id}`, {
    headers: getHeaders(token)
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.message || 'Failed to fetch task')
  return data
}

export async function createTask(token, task) {
  const res = await fetch(TASKS_BASE, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify(task)
  })
  const data = await res.json()
  if (!res.ok) {
    const msg = data.errors ? data.errors.join(', ') : data.message
    throw new Error(msg || 'Failed to create task')
  }
  return data
}

export async function updateTask(token, id, task) {
  const res = await fetch(`${TASKS_BASE}/${id}`, {
    method: 'PUT',
    headers: getHeaders(token),
    body: JSON.stringify(task)
  })
  const data = await res.json()
  if (!res.ok) {
    const msg = data.errors ? data.errors.join(', ') : data.message
    throw new Error(msg || 'Failed to update task')
  }
  return data
}

export async function patchTask(token, id, fields) {
  const res = await fetch(`${TASKS_BASE}/${id}`, {
    method: 'PATCH',
    headers: getHeaders(token),
    body: JSON.stringify(fields)
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.message || 'Failed to patch task')
  return data
}

export async function deleteTask(token, id) {
  const res = await fetch(`${TASKS_BASE}/${id}`, {
    method: 'DELETE',
    headers: getHeaders(token)
  })
  if (!res.ok) {
    if (res.status === 403) throw new Error('Only admins can delete tasks')
    throw new Error('Failed to delete task')
  }
}
