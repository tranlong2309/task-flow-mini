import { useState } from 'react'
import { MoreHorizontal, Plus, X } from 'lucide-react'
import { useDroppable } from '@dnd-kit/core'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import type { BoardColumn, Task, User } from '../../types/domain'
import { DraggableTask } from '../tasks/DraggableTask'
import { taskService } from '../../lib/api/services'
import { useAppStore } from '../../stores/useAppStore'

interface KanbanColumnProps {
  column: BoardColumn
  tasks: Task[]
  users: User[]
  onTaskClick: (id: string | number) => void
}

export function KanbanColumn({ column, tasks, users, onTaskClick }: KanbanColumnProps) {
  const { setNodeRef, isOver } = useDroppable({ id: column.id })
  const [isAdding, setIsAdding] = useState(false)
  const [newTaskTitle, setNewTaskTitle] = useState('')
  const qc = useQueryClient()
  const { selectedProjectId } = useAppStore()

  const createMutation = useMutation({
    mutationFn: (title: string) => taskService.create({
      title,
      description: '',
      assigneeId: users[0]?.id,
      priority: 'MEDIUM',
      boardId: selectedProjectId,
      projectId: selectedProjectId,
      statusColumnId: column.id,
      dueDate: new Date(Date.now() + 7 * 86400000).toISOString()
    }),
    onMutate: async (title) => {
      await qc.cancelQueries({ queryKey: ['board', selectedProjectId] })
      const previousBoard = qc.getQueryData(['board', selectedProjectId])
      
      qc.setQueryData(['board', selectedProjectId], (old: any) => {
        if (!old) return old
        const optimisticTask = {
          id: `temp-${Date.now()}`,
          title,
          description: '',
          assigneeId: users[0]?.id,
          priority: 'MEDIUM',
          statusColumnId: column.id,
          boardId: selectedProjectId,
          createdAt: new Date().toISOString(),
          updatedAt: 'Just now',
          tags: []
        }
        return {
          ...old,
          tasks: [...old.tasks, optimisticTask]
        }
      })
      
      return { previousBoard }
    },
    onError: (_err, _newTitle, context) => {
      qc.setQueryData(['board', selectedProjectId], context?.previousBoard)
    },
    onSettled: () => {
      qc.invalidateQueries({ queryKey: ['board', selectedProjectId] })
    }
  })

  const handleAddSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!newTaskTitle.trim()) return
    createMutation.mutate(newTaskTitle.trim())
    setNewTaskTitle('')
    setIsAdding(false)
  }
  
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
        
        {isAdding ? (
          <form onSubmit={handleAddSubmit} className="inline-add-task" style={{
            background: 'white',
            padding: '10px',
            borderRadius: '8px',
            border: '1px solid #e7e6ed',
            boxShadow: '0 2px 4px rgba(0,0,0,0.02)',
            marginTop: '8px'
          }}>
            <input
              autoFocus
              value={newTaskTitle}
              onChange={(e) => setNewTaskTitle(e.target.value)}
              placeholder="What needs to be done?"
              style={{
                width: '100%',
                border: 'none',
                outline: 'none',
                fontSize: '12px',
                color: '#39374b',
                marginBottom: '8px'
              }}
            />
            <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: '6px' }}>
              <button 
                type="button" 
                className="icon-button" 
                onClick={() => { setIsAdding(false); setNewTaskTitle('') }}
                style={{ padding: '4px' }}
              >
                <X size={14} />
              </button>
              <button 
                type="submit" 
                className="button primary" 
                disabled={!newTaskTitle.trim() || createMutation.isPending}
                style={{ padding: '4px 10px', fontSize: '11px', minHeight: 'auto' }}
              >
                Add
              </button>
            </div>
          </form>
        ) : (
          <button className="add-task" onClick={() => setIsAdding(true)}>
            <Plus size={15} /> Add task
          </button>
        )}
      </div>
    </section>
  )
}
