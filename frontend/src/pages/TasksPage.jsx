import { useState, useEffect, useCallback, useRef } from 'react'
import { useAuth } from '../context/AuthContext'
import { getTasks, createTask, updateTask, deleteTask, patchTask } from '../api/api'
import TaskCard from '../components/TaskCard'
import TaskForm from '../components/TaskForm'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

const STATUSES = ['ALL', 'TODO', 'IN_PROGRESS', 'DONE']

export default function TasksPage() {
  const { token, role, username, logout } = useAuth()
  const [tasks, setTasks] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [showForm, setShowForm] = useState(false)
  const [editingTask, setEditingTask] = useState(null)
  const [error, setError] = useState('')
  const [notification, setNotification] = useState('')
  const stompClient = useRef(null)

  const loadTasks = useCallback(async () => {
    try {
      const status = filter === 'ALL' ? undefined : filter
      const data = await getTasks(token, { status, page, size: 10 })
      setTasks(data.content)
      setTotalPages(data.totalPages)
      setError('')
    } catch (err) {
      setError(err.message)
    }
  }, [token, filter, page])

  useEffect(() => {
    loadTasks()
  }, [loadTasks])

  // WebSocket connection for real-time updates
  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe('/topic/tasks', (message) => {
          const event = JSON.parse(message.body)
          setNotification(`Task "${event.task.title}" ${event.action.toLowerCase()}`)
          setTimeout(() => setNotification(''), 3000)
          loadTasks()
        })
      }
    })

    client.activate()
    stompClient.current = client

    return () => {
      if (client.active) client.deactivate()
    }
  }, [loadTasks])

  async function handleCreate(task) {
    try {
      await createTask(token, task)
      setShowForm(false)
      setError('')
      await loadTasks()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleUpdate(task) {
    try {
      await updateTask(token, editingTask.id, task)
      setEditingTask(null)
      setShowForm(false)
      setError('')
      await loadTasks()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleDelete(id) {
    if (!confirm('Are you sure you want to delete this task?')) return
    try {
      await deleteTask(token, id)
      setError('')
      await loadTasks()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleStatusChange(id, newStatus) {
    try {
      await patchTask(token, id, { status: newStatus })
      setError('')
      await loadTasks()
    } catch (err) {
      setError(err.message)
    }
  }

  function openEditForm(task) {
    setEditingTask(task)
    setShowForm(true)
  }

  function closeForm() {
    setShowForm(false)
    setEditingTask(null)
  }

  return (
    <div className="tasks-page">
      <header className="header">
        <div className="header-left">
          <h1>Task Manager</h1>
        </div>
        <div className="header-right">
          <span className="user-info">{username} <span className="badge">{role}</span></span>
          <button className="btn-logout" onClick={logout}>Logout</button>
        </div>
      </header>

      {notification && <div className="notification">{notification}</div>}
      {error && <div className="error-banner">{error}</div>}

      <div className="toolbar">
        <div className="filters">
          {STATUSES.map(s => (
            <button
              key={s}
              className={`filter-btn ${filter === s ? 'active' : ''}`}
              onClick={() => { setFilter(s); setPage(0) }}
            >
              {s.replace('_', ' ')}
            </button>
          ))}
        </div>
        <button className="btn-primary" onClick={() => { setEditingTask(null); setShowForm(true) }}>
          + New Task
        </button>
      </div>

      {showForm && (
        <TaskForm
          task={editingTask}
          onSubmit={editingTask ? handleUpdate : handleCreate}
          onCancel={closeForm}
        />
      )}

      <div className="task-list">
        {tasks.length === 0 ? (
          <p className="empty">No tasks found.</p>
        ) : (
          tasks.map(task => (
            <TaskCard
              key={task.id}
              task={task}
              isAdmin={role === 'ADMIN'}
              onEdit={() => openEditForm(task)}
              onDelete={() => handleDelete(task.id)}
              onStatusChange={handleStatusChange}
            />
          ))
        )}
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>Previous</button>
          <span>Page {page + 1} of {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</button>
        </div>
      )}
    </div>
  )
}
