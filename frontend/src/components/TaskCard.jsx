const STATUS_OPTIONS = ['TODO', 'IN_PROGRESS', 'DONE']

const STATUS_COLORS = {
  TODO: '#6b7280',
  IN_PROGRESS: '#f59e0b',
  DONE: '#10b981'
}

export default function TaskCard({ task, isAdmin, onEdit, onDelete, onStatusChange }) {
  function formatDate(dateStr) {
    if (!dateStr) return ''
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
    })
  }

  return (
    <div className="task-card">
      <div className="task-header">
        <h3 className="task-title">{task.title}</h3>
        <select
          className="status-select"
          value={task.status || 'TODO'}
          onChange={e => onStatusChange(task.id, e.target.value)}
          style={{ borderColor: STATUS_COLORS[task.status] }}
        >
          {STATUS_OPTIONS.map(s => (
            <option key={s} value={s}>{s.replace('_', ' ')}</option>
          ))}
        </select>
      </div>

      {task.description && (
        <p className="task-description">{task.description}</p>
      )}

      <div className="task-footer">
        <span className="task-date">
          Created {formatDate(task.createdAt)}
          {task.updatedAt !== task.createdAt && ` \u00b7 Updated ${formatDate(task.updatedAt)}`}
        </span>
        <div className="task-actions">
          <button className="btn-edit" onClick={onEdit}>Edit</button>
          {isAdmin && (
            <button className="btn-delete" onClick={onDelete}>Delete</button>
          )}
        </div>
      </div>
    </div>
  )
}
