import { CalendarDays, MessageSquare, CheckSquare, Paperclip } from 'lucide-react'
import type { Task, User } from '../../types/domain'
import { Avatar } from '../../components/ui/Avatar'

interface TaskCardProps {
  task: Task
  users?: User[]
  overlay?: boolean
}

export function TaskCard({ task, users = [], overlay = false }: TaskCardProps) {
  const formattedDate = task.dueDate 
    ? new Date(task.dueDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
    : ''
    
  const completedSubtasks = task.subtasks?.filter(s => s.isCompleted).length || 0
  const totalSubtasks = task.subtasks?.length || 0
  
  return (
    <div className={`task-card ${overlay ? 'overlay-card' : ''}`}>
      <div className="task-card-top">
        <span className={`priority ${task.priority.toLowerCase()}`}>
          <span />
          {task.priority}
        </span>
      </div>
      <h3>{task.title}</h3>
      {task.description && <p>{task.description}</p>}
      <div className="task-badges" style={{ display: 'flex', gap: '10px', fontSize: '10px', color: '#9997a8', marginBottom: '10px' }}>
        {totalSubtasks > 0 && (
          <span style={{ display: 'flex', alignItems: 'center', gap: '3px' }} title="Subtasks">
            <CheckSquare size={12} />
            {completedSubtasks}/{totalSubtasks}
          </span>
        )}
        {task.comments && task.comments.length > 0 && (
          <span style={{ display: 'flex', alignItems: 'center', gap: '3px' }} title="Comments">
            <MessageSquare size={12} />
            {task.comments.length}
          </span>
        )}
        {task.attachments && task.attachments.length > 0 && (
          <span style={{ display: 'flex', alignItems: 'center', gap: '3px' }} title="Attachments">
            <Paperclip size={12} />
            {task.attachments.length}
          </span>
        )}
      </div>
      <div className="task-footer">
        {formattedDate && (
          <span className="due">
            <CalendarDays size={14} />
            {formattedDate}
          </span>
        )}
        <div className="avatar-group" style={{ display: 'flex', flexDirection: 'row-reverse' }}>
          {users.length > 0 ? (
            users.map((u, i) => (
              <div key={u.id} style={{ marginLeft: i > 0 ? '-8px' : '0', position: 'relative', zIndex: i }}>
                <Avatar user={u} small />
              </div>
            ))
          ) : (
            <Avatar small />
          )}
        </div>
      </div>
    </div>
  ) 
}
