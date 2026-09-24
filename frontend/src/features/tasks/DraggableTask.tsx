import { CalendarDays, MoreHorizontal } from 'lucide-react'
import { useDraggable } from '@dnd-kit/core'
import type { Task, User } from '../../types/domain'
import { Avatar } from '../../components/ui/Avatar'

interface DraggableTaskProps {
  task: Task
  user?: User
  onClick: () => void
}

export function DraggableTask({ task, user, onClick }: DraggableTaskProps) {
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({ id: task.id })
  
  return (
    <article 
      ref={setNodeRef} 
      style={{ transform: transform ? `translate3d(${transform.x}px, ${transform.y}px, 0)` : undefined }} 
      className={`task-card ${isDragging ? 'dragging' : ''}`} 
      {...listeners} 
      {...attributes} 
      onClick={onClick}
    >
      <div className="task-card-top">
        <span className={`priority ${task.priority.toLowerCase()}`}>
          <span />{task.priority}
        </span>
        <button className="plain-button" onClick={(event) => event.stopPropagation()}>
          <MoreHorizontal size={16} />
        </button>
      </div>
      <h3>{task.title}</h3>
      <p>{task.description}</p>
      <div className="tag-row">
        {task.tags?.map((tag) => <span className="tag" key={tag}>{tag}</span>)}
      </div>
      <div className="task-footer">
        <span className={task.blocked ? 'due overdue' : 'due'}>
          <CalendarDays size={14} />
          {task.dueDate}
        </span>
        <Avatar user={user} small />
      </div>
      {task.blocked && <div className="blocked-note">Blocked · {task.blocker}</div>}
    </article>
  )
}
