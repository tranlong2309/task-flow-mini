import { useState } from 'react'
import { X } from 'lucide-react'
import type { User } from '../../types/domain'
import { taskService } from '../../lib/api/services'
import { useAppStore } from '../../stores/useAppStore'

interface CreateTaskProps {
  users: User[]
  onClose: () => void
  onCreated: () => void
}

export function CreateTask({ users, onClose, onCreated }: CreateTaskProps) { 
  const [title, setTitle] = useState('')
  const [assignee, setAssignee] = useState<string | number>(users[1]?.id ?? 2)
  const [saving, setSaving] = useState(false)
  
  return (
    <div className="modal-backdrop">
      <form 
        className="modal" 
        onSubmit={async (event) => { 
          event.preventDefault()
          if (!title.trim()) return
          setSaving(true)
          await taskService.create({ 
            title, 
            description: 'New task created from the board.', 
            assigneeId: assignee, 
            priority: 'MEDIUM', 
            dueDate: '2026-10-04T17:00:00Z', 
            boardId: useAppStore.getState().selectedProjectId,
            projectId: useAppStore.getState().selectedProjectId,
            statusColumnId: 1
          })
          onCreated() 
        }}
      >
        <div className="modal-head">
          <div>
            <span className="eyebrow">New task</span>
            <h2>Create a task</h2>
          </div>
          <button type="button" className="icon-button" onClick={onClose}>
            <X size={18} />
          </button>
        </div>
        <label>
          Task title
          <input 
            autoFocus 
            value={title} 
            onChange={(event) => setTitle(event.target.value)} 
            placeholder="What needs to be done?" 
            required 
          />
        </label>
        <label>
          Assignee
          <select value={assignee} onChange={(event) => setAssignee(event.target.value)}>
            {users.filter((user) => user.role !== 'ADMIN').map((user) => (
              <option value={user.id} key={user.id}>{user.name}</option>
            ))}
          </select>
        </label>
        <label>
          Due date
          <input type="date" defaultValue="2026-10-04" />
        </label>
        <div className="modal-actions">
          <button type="button" className="button secondary" onClick={onClose}>Cancel</button>
          <button className="button primary" disabled={saving}>
            {saving ? 'Creating...' : 'Create task'}
          </button>
        </div>
      </form>
    </div>
  ) 
}
