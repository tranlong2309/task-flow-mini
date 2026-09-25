import { useState, useRef } from 'react'
import { Check, X, CalendarDays, Plus, MessageSquare, Paperclip, CheckSquare } from 'lucide-react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import type { Task, User, BoardColumn, Subtask, Comment, Attachment } from '../../types/domain'
import { taskService } from '../../lib/api/services'
import { Avatar } from '../../components/ui/Avatar'

interface TaskDrawerProps {
  task: Task
  user?: User
  column?: BoardColumn
  canEdit: boolean
  onClose: () => void
}

export function TaskDrawer({ task, user, column, canEdit, onClose }: TaskDrawerProps) {
  const queryClient = useQueryClient()
  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: () => taskService.listUsers()
  })
  
  const [editing, setEditing] = useState(false)
  const [title, setTitle] = useState(task.title)
  const [description, setDescription] = useState(task.description || '')
  
  const [assignedDate, setAssignedDate] = useState(task.assignedDate?.split('T')[0] || '')
  const [startDate, setStartDate] = useState(task.startDate?.split('T')[0] || '')
  const [dueDate, setDueDate] = useState(task.dueDate?.split('T')[0] || '')
  const [priority, setPriority] = useState(task.priority || 'MEDIUM')
  const [selectedAssignees, setSelectedAssignees] = useState<Set<number | string>>(
    new Set(task.assigneeIds || [])
  )
  
  const [newComment, setNewComment] = useState('')
  const [newSubtask, setNewSubtask] = useState('')
  const fileInputRef = useRef<HTMLInputElement>(null)

  const allUsers = usersQuery.data || []
  const assignees = allUsers.filter(u => task.assigneeIds?.includes(Number(u.id)) || String(task.assigneeId) === String(u.id))
  
  const saveMutation = useMutation({ 
    mutationFn: () => taskService.update(task.id, { 
      title, 
      description,
      priority,
      assignedDate: assignedDate ? `${assignedDate}T00:00:00Z` : undefined,
      startDate: startDate ? `${startDate}T00:00:00Z` : undefined,
      dueDate: dueDate ? `${dueDate}T00:00:00Z` : undefined,
      assigneeIds: Array.from(selectedAssignees),
      comments: task.comments,
      subtasks: task.subtasks
    }), 
    onSuccess: () => { 
      setEditing(false)
      queryClient.invalidateQueries({ queryKey: ['board'] }) 
    } 
  })
  
  const addComment = () => {
    if (!newComment.trim()) return
    const comment: Comment = {
      id: crypto.randomUUID(),
      userId: user?.id || 1, 
      content: newComment,
      createdAt: new Date().toISOString()
    }
    const updatedComments = [...(task.comments || []), comment]
    taskService.update(task.id, { comments: updatedComments }).then(() => {
      setNewComment('')
      queryClient.invalidateQueries({ queryKey: ['board'] })
    })
  }
  
  const addSubtask = () => {
    if (!newSubtask.trim()) return
    const subtask: Subtask = {
      id: crypto.randomUUID(),
      title: newSubtask,
      isCompleted: false,
      createdAt: new Date().toISOString()
    }
    const updatedSubtasks = [...(task.subtasks || []), subtask]
    taskService.update(task.id, { subtasks: updatedSubtasks }).then(() => {
      setNewSubtask('')
      queryClient.invalidateQueries({ queryKey: ['board'] })
    })
  }

  const toggleSubtask = (subtaskId: string) => {
    const updatedSubtasks = (task.subtasks || []).map(st => 
      st.id === subtaskId ? { ...st, isCompleted: !st.isCompleted } : st
    )
    taskService.update(task.id, { subtasks: updatedSubtasks }).then(() => {
      queryClient.invalidateQueries({ queryKey: ['board'] })
    })
  }

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    const att: Attachment = {
      id: crypto.randomUUID(),
      fileName: file.name,
      fileUrl: URL.createObjectURL(file), // mock url for demo
      uploadedBy: user?.id || 1,
      createdAt: new Date().toISOString()
    }
    const updated = [...(task.attachments || []), att]
    taskService.update(task.id, { attachments: updated }).then(() => {
      queryClient.invalidateQueries({ queryKey: ['board'] })
      if (fileInputRef.current) fileInputRef.current.value = ''
    })
  }
  
  return (
    <>
      <div className="modal-backdrop" onClick={onClose} style={{ zIndex: 15 }} />
      <aside className="drawer" style={{ overflowY: 'auto' }}>
        <div className="drawer-head">
          <span className="eyebrow">Task details · Project #{task.projectId}</span>
          <button className="icon-button" onClick={onClose}>
            <X size={18} />
          </button>
        </div>
        
        {editing ? (
          <div className="drawer-edit-mode">
            <input 
              className="drawer-edit-input" 
              value={title} 
              onChange={(e) => setTitle(e.target.value)} 
              placeholder="Task title"
            />
            <textarea 
              className="drawer-edit-input" 
              value={description} 
              onChange={(e) => setDescription(e.target.value)} 
              placeholder="Add a more detailed description..."
              rows={4}
            />
          </div>
        ) : (
          <div style={{ marginBottom: '24px' }}>
            <h2 
              onClick={() => canEdit && setEditing(true)} 
              style={{ cursor: canEdit ? 'pointer' : 'default' }}
              title={canEdit ? 'Click to edit' : ''}
            >
              {task.title}
            </h2>
            <p 
              className="drawer-description"
              onClick={() => canEdit && setEditing(true)} 
              style={{ cursor: canEdit ? 'pointer' : 'default' }}
              title={canEdit ? 'Click to edit' : ''}
            >
              {task.description || <span className="text-gray-400">No description provided. Click to add one.</span>}
            </p>
          </div>
        )}
        
        <div className="drawer-actions">
          {canEdit && (
            editing ? (
              <button className="button primary" onClick={() => saveMutation.mutate()} disabled={saveMutation.isPending}>
                Save Changes
              </button>
            ) : (
              <button className="button secondary" onClick={() => setEditing(true)}>
                Edit task
              </button>
            )
          )}
        </div>
        
        <div className="drawer-grid">
          <div className="drawer-row">
            <span>Status</span>
            <strong>
              <span className={`column-dot`} style={{ background: column?.tone || '#ccc' }} />
              {column?.name || 'Unknown'}
            </strong>
          </div>
          
          <div 
            className="drawer-row"
            onClick={() => canEdit && !editing && setEditing(true)} 
            style={{ cursor: canEdit && !editing ? 'pointer' : 'default' }}
            title={canEdit && !editing ? 'Click to edit' : ''}
          >
            <span>Priority</span>
            {editing ? (
              <select className="date-input" value={priority} onChange={e => setPriority(e.target.value as any)} onClick={e => e.stopPropagation()}>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            ) : (
              <strong className={`priority ${task.priority.toLowerCase()}`}>
                <span />{task.priority}
              </strong>
            )}
          </div>

          <div 
            className="drawer-row"
            onClick={() => canEdit && !editing && setEditing(true)} 
            style={{ cursor: canEdit && !editing ? 'pointer' : 'default' }}
            title={canEdit && !editing ? 'Click to edit' : ''}
          >
            <span>Assignees</span>
            {editing ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', width: '100%', maxWidth: '200px', alignItems: 'flex-end' }} onClick={e => e.stopPropagation()}>
                <select 
                  className="date-input"
                  style={{ width: '100%' }}
                  onChange={e => {
                    if (e.target.value) {
                      const id = isNaN(Number(e.target.value)) ? e.target.value : Number(e.target.value);
                      setSelectedAssignees(prev => {
                        const next = new Set(prev);
                        next.add(id);
                        return next;
                      });
                    }
                  }}
                  value=""
                >
                  <option value="">+ Assign User</option>
                  {allUsers.filter(u => !selectedAssignees.has(u.id)).map(u => (
                    <option key={u.id} value={u.id}>{u.name}</option>
                  ))}
                </select>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px', justifyContent: 'flex-end' }}>
                  {Array.from(selectedAssignees).map(id => {
                    const u = allUsers.find(user => user.id === id);
                    return u ? (
                      <div key={u.id} style={{ position: 'relative', cursor: 'pointer' }} onClick={(e) => {
                        e.stopPropagation();
                        setSelectedAssignees(prev => {
                          const next = new Set(prev);
                          next.delete(u.id);
                          return next;
                        });
                      }}>
                        <Avatar user={u} small />
                      </div>
                    ) : null;
                  })}
                </div>
              </div>
            ) : (
              <div style={{ display: 'flex', gap: '4px' }}>
                {assignees.length > 0 ? (
                  assignees.map(a => <Avatar key={a.id} user={a} small />)
                ) : (
                  <span className="text-gray-400">Unassigned</span>
                )}
              </div>
            )}
          </div>
          
          {editing ? (
            <>
              <div className="drawer-row">
                <span>Start Date</span>
                <input type="date" className="date-input" value={startDate} onChange={e => setStartDate(e.target.value)} />
              </div>
              <div className="drawer-row">
                <span>Due Date</span>
                <input type="date" className="date-input" value={dueDate} onChange={e => setDueDate(e.target.value)} />
              </div>
            </>
          ) : (
            <>
              {task.startDate && (
                <div 
                  className="drawer-row" 
                  onClick={() => canEdit && setEditing(true)} 
                  style={{ cursor: canEdit ? 'pointer' : 'default' }}
                  title={canEdit ? 'Click to edit' : ''}
                >
                  <span>Start Date</span>
                  <strong><CalendarDays size={14} /> {task.startDate.split('T')[0]}</strong>
                </div>
              )}
              <div 
                className="drawer-row" 
                onClick={() => canEdit && setEditing(true)} 
                style={{ cursor: canEdit ? 'pointer' : 'default' }}
                title={canEdit ? 'Click to edit' : ''}
              >
                <span>Due Date</span>
                <strong><CalendarDays size={14} /> {task.dueDate?.split('T')[0] || 'None'}</strong>
              </div>
            </>
          )}
        </div>
        
        {task.blocked && (
          <div className="drawer-blocked">
            <strong>Blocked</strong>
            <p>{task.blocker}</p>
          </div>
        )}
        
        {/* Subtasks Section */}
        <div className="task-section">
          <div className="section-inline">
            <h3><CheckSquare size={16} style={{ display: 'inline', verticalAlign: 'text-bottom' }} /> Subtasks</h3>
          </div>
          <div className="subtask-list">
            {(task.subtasks || []).map(st => (
              <label key={st.id} className="subtask-item" style={{ cursor: 'pointer' }}>
                <input 
                  type="checkbox" 
                  checked={st.isCompleted} 
                  onChange={() => toggleSubtask(st.id)} 
                  disabled={!canEdit}
                />
                <span className={st.isCompleted ? 'line-through text-gray-400' : ''}>{st.title}</span>
              </label>
            ))}
            {canEdit && (
              <div className="add-inline-form">
                <input 
                  value={newSubtask} 
                  onChange={e => setNewSubtask(e.target.value)} 
                  placeholder="Add a new subtask..." 
                  onKeyDown={e => e.key === 'Enter' && addSubtask()}
                />
                <button className="icon-button" style={{ background: '#f0eff3' }} onClick={addSubtask}>
                  <Plus size={16} />
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Attachments Section */}
        <div className="task-section">
          <div className="section-inline">
            <h3><Paperclip size={16} style={{ display: 'inline', verticalAlign: 'text-bottom' }} /> Attachments</h3>
            {canEdit && (
              <label className="plain-button" style={{ cursor: 'pointer' }} title="Upload file">
                <Plus size={16} />
                <input type="file" style={{ display: 'none' }} ref={fileInputRef} onChange={handleFileUpload} />
              </label>
            )}
          </div>
          <div className="attachment-list">
            {(task.attachments || []).map(att => (
              <div key={att.id} className="subtask-item" style={{ gap: '8px' }}>
                <Paperclip size={14} style={{ color: '#5954c8' }} />
                <a href={att.fileUrl} target="_blank" rel="noreferrer" style={{ color: '#49475d', textDecoration: 'none', fontWeight: 600 }}>
                  {att.fileName}
                </a>
              </div>
            ))}
            {(!task.attachments || task.attachments.length === 0) && (
              <p className="text-gray-400" style={{ fontSize: '11px', marginTop: '10px' }}>No files attached.</p>
            )}
          </div>
        </div>

        {/* Comments Section */}
        <div className="task-section">
          <div className="section-inline">
            <h3><MessageSquare size={16} style={{ display: 'inline', verticalAlign: 'text-bottom' }} /> Activity</h3>
          </div>
          <div className="comment-list" style={{ marginTop: '16px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {(task.comments || []).map(comment => {
              const author = allUsers.find(u => String(u.id) === String(comment.userId))
              return (
                <div key={comment.id} style={{ display: 'flex', gap: '10px' }}>
                  <Avatar user={author} small />
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', gap: '8px', alignItems: 'baseline', marginBottom: '4px' }}>
                      <strong style={{ fontSize: '12px', color: '#39374b' }}>{author?.name || 'Unknown'}</strong>
                      <small style={{ fontSize: '10px', color: '#9997a8' }}>{new Date(comment.createdAt).toLocaleString()}</small>
                    </div>
                    <div style={{ background: '#f8f8fa', padding: '10px', borderRadius: '0 10px 10px 10px', fontSize: '12px', color: '#575568', border: '1px solid #f0eff3' }}>
                      {comment.content}
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
          
          {canEdit && (
            <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
              <Avatar user={user} small />
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <textarea 
                  className="drawer-edit-input"
                  style={{ marginTop: 0, minHeight: '60px' }}
                  value={newComment} 
                  onChange={e => setNewComment(e.target.value)} 
                  placeholder="Write a comment..."
                  rows={2}
                />
                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <button className="button primary" onClick={addComment} disabled={!newComment.trim()}>
                    Comment
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
        
      </aside>
    </>
  )
}
