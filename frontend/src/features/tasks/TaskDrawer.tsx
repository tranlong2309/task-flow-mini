import { useState } from 'react'
import { Check, X, CalendarDays, Plus } from 'lucide-react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import type { Task, User, BoardColumn } from '../../types/domain'
import { taskService, ticketService } from '../../lib/api/services'
import { Avatar } from '../../components/ui/Avatar'
import { TicketRow } from './TicketRow'

interface TaskDrawerProps {
  task: Task
  user?: User
  column?: BoardColumn
  canEdit: boolean
  onClose: () => void
}

export function TaskDrawer({ task, user, column, canEdit, onClose }: TaskDrawerProps) {
  const queryClient = useQueryClient()
  const ticketsQuery = useQuery({ 
    queryKey: ['tickets', task.id], 
    queryFn: () => ticketService.list(task.id) 
  })
  
  const [showTicketForm, setShowTicketForm] = useState(false)
  const [editing, setEditing] = useState(false)
  const [title, setTitle] = useState(task.title)
  const [description, setDescription] = useState(task.description)
  
  const saveMutation = useMutation({ 
    mutationFn: () => taskService.update(task.id, { title, description }), 
    onSuccess: () => { 
      setEditing(false)
      queryClient.invalidateQueries({ queryKey: ['board', task.projectId] }) 
    } 
  })
  
  const ticketMutation = useMutation({ 
    mutationFn: () => ticketService.create({ 
      taskId: task.id, 
      title: 'New ticket', 
      description: 'Ticket details', 
      statusColumnId: task.columnId ?? 1, 
      priority: 'MEDIUM', 
      assigneeId: task.assigneeId ?? 1, 
      dueDate: task.dueDate 
    }), 
    onSuccess: () => { 
      setShowTicketForm(false)
      queryClient.invalidateQueries({ queryKey: ['tickets', task.id] }) 
    } 
  })
  
  return (
    <aside className="drawer">
      <div className="drawer-head">
        <span className="eyebrow">Task details · Project #{task.projectId}</span>
        <button className="icon-button" onClick={onClose}>
          <X size={18} />
        </button>
      </div>
      
      {editing ? (
        <>
          <input 
            className="drawer-edit-input" 
            value={title} 
            onChange={(event) => setTitle(event.target.value)} 
          />
          <textarea 
            className="drawer-edit-input" 
            value={description} 
            onChange={(event) => setDescription(event.target.value)} 
          />
        </>
      ) : (
        <>
          <h2>{task.title}</h2>
          <p className="drawer-description">{task.description}</p>
        </>
      )}
      
      <div className="drawer-actions">
        {canEdit && (
          editing ? (
            <button className="button primary" onClick={() => saveMutation.mutate()} disabled={saveMutation.isPending}>
              Save
            </button>
          ) : (
            <button className="button secondary" onClick={() => setEditing(true)}>
              Edit task
            </button>
          )
        )}
      </div>
      
      <div className="drawer-row">
        <span>Status</span>
        <strong>
          <span className={`column-dot ${column?.tone}`} />
          {column?.name}
        </strong>
      </div>
      
      <div className="drawer-row">
        <span>Assignee</span>
        <strong>
          <Avatar user={user} small /> {user?.name}
        </strong>
      </div>
      
      <div className="drawer-row">
        <span>Priority</span>
        <strong className={`priority ${task.priority.toLowerCase()}`}>
          <span />{task.priority}
        </strong>
      </div>
      
      <div className="drawer-row">
        <span>Due date</span>
        <strong>
          <CalendarDays size={15} /> {task.dueDate}
        </strong>
      </div>
      
      {task.blocked && (
        <div className="drawer-blocked">
          <strong>Blocked</strong>
          <p>{task.blocker}</p>
        </div>
      )}
      
      <div className="ticket-section">
        <div className="section-inline">
          <h3>Tickets <span>{ticketsQuery.data?.length ?? 0}</span></h3>
          {canEdit && (
            <button className="plain-button" onClick={() => setShowTicketForm(true)}>
              <Plus size={16} />
            </button>
          )}
        </div>
        
        {showTicketForm && (
          <div className="ticket-form">
            <input defaultValue="New ticket" aria-label="Ticket title" />
            <button className="button primary" onClick={() => ticketMutation.mutate()}>Add</button>
          </div>
        )}
        
        {ticketsQuery.data?.map((ticket) => (
          <TicketRow key={ticket.id} ticket={ticket} canEdit={canEdit} />
        ))}
      </div>
      
      <div className="activity">
        <h3>Activity</h3>
        <div className="activity-item">
          <span className="activity-line" />
          <Avatar user={user} small />
          <p>
            <strong>{user?.name}</strong> updated this task
            <small>{task.updatedAt}</small>
          </p>
        </div>
      </div>
      
      <button className="button primary full">
        <Check size={16} /> Mark as complete
      </button>
    </aside>
  )
}
