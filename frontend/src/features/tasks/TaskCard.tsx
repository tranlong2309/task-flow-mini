import { CalendarDays } from 'lucide-react'
import type { Task, User } from '../../types/domain'
import { Avatar } from '../../components/ui/Avatar'

interface TaskCardProps {
  task: Task
  user?: User
  overlay?: boolean
}

export function TaskCard({ task, user, overlay = false }: TaskCardProps) { 
  return (
    <div className={`task-card ${overlay ? 'overlay-card' : ''}`}>
      <div className="task-card-top">
        <span className={`priority ${task.priority.toLowerCase()}`}>
          <span />
          {task.priority}
        </span>
      </div>
      <h3>{task.title}</h3>
      <div className="task-footer">
        <span className="due">
          <CalendarDays size={14} />
          {task.dueDate}
        </span>
        <Avatar user={user} small />
      </div>
    </div>
  ) 
}
