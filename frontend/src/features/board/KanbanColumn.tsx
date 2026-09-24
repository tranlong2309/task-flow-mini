import { MoreHorizontal, Plus } from 'lucide-react'
import { useDroppable } from '@dnd-kit/core'
import type { BoardColumn, Task, User } from '../../types/domain'
import { DraggableTask } from '../tasks/DraggableTask'

interface KanbanColumnProps {
  column: BoardColumn
  tasks: Task[]
  users: User[]
  onTaskClick: (id: string | number) => void
}

export function KanbanColumn({ column, tasks, users, onTaskClick }: KanbanColumnProps) {
  const { setNodeRef, isOver } = useDroppable({ id: column.id })
  
  return (
    <section ref={setNodeRef} className={`kanban-column ${isOver ? 'drop-active' : ''}`}>
      <div className="column-head">
        <div>
          <span 
            className="column-dot" 
            style={{ background: column.tone && column.tone.startsWith('#') ? column.tone : undefined }}
          />
          <strong>{column.name}</strong>
          <span className="task-count">{tasks.length}</span>
        </div>
        <button className="plain-button">
          <MoreHorizontal size={17} />
        </button>
      </div>
      <div className="column-body">
        {tasks.map((task) => (
          <DraggableTask 
            key={task.id} 
            task={task} 
            user={users.find((item) => item.id === task.assigneeId)} 
            onClick={() => onTaskClick(task.id)} 
          />
        ))}
        {tasks.length === 0 && <div className="column-empty">Drop tasks here</div>}
        <button className="add-task" onClick={() => onTaskClick(0)}>
          <Plus size={15} /> Add task
        </button>
      </div>
    </section>
  )
}
