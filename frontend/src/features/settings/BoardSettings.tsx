import { useState, useEffect } from 'react'
import { Check, Plus, Trash } from 'lucide-react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import type { DragEndEvent } from '@dnd-kit/core'
import {
  SortableContext,
  arrayMove,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
  useSortable
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'

import type { Board, BoardColumn } from '../../types/domain'
import { boardService } from '../../lib/api/services'

function SortableColumnRow({ 
  column, 
  index, 
  updateColumn, 
  removeColumn 
}: { 
  column: BoardColumn & { tempId?: string | number }
  index: number
  updateColumn: (id: string | number, updater: Partial<BoardColumn>) => void
  removeColumn: (id: string | number) => void
}) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: column.tempId || column.id })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    zIndex: isDragging ? 1 : 0,
    opacity: isDragging ? 0.5 : 1,
    background: isDragging ? '#f8f8fb' : 'transparent',
  }

  return (
    <div className="setting-row" ref={setNodeRef} style={style}>
      <span className="drag-handle" {...attributes} {...listeners}>⠿</span>
      
      <span 
        className="column-dot" 
        style={{ 
          background: column.tone && column.tone.startsWith('#') ? column.tone : undefined,
          marginRight: -2 
        }}
      />
      
      <input 
        type="color"
        className="input" 
        style={{ width: 40, height: 40, padding: 0, marginRight: 10, cursor: 'pointer', border: 0, background: 'transparent' }}
        value={column.tone && column.tone.startsWith('#') ? column.tone : '#aaa8b7'}
        onChange={(e) => updateColumn(column.tempId || column.id, { tone: e.target.value })}
        title="Pick a color"
      />
      
      <input 
        value={column.name} 
        onChange={(e) => updateColumn(column.tempId || column.id, { name: e.target.value })} 
      />
      
      <span className="setting-order">Position {index + 1}</span>
      <button className="plain-button" onClick={() => removeColumn(column.tempId || column.id)}>
        <Trash size={17} color="red" />
      </button>
    </div>
  )
}

export function BoardSettings({ board }: { board: Board }) {
  const queryClient = useQueryClient()
  const [columns, setColumns] = useState<(BoardColumn & { tempId?: string | number })[]>(() => {
    const sorted = [...board.columns].sort((a, b) => a.order - b.order)
    return sorted.map(c => ({ ...c, tempId: c.id }))
  })
  
  useEffect(() => {
    const sorted = [...board.columns].sort((a, b) => a.order - b.order)
    setColumns(sorted.map(c => ({ ...c, tempId: c.id })))
  }, [board.columns])
  
  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  )

  const saveMutation = useMutation({
    mutationFn: () => boardService.updateColumns(board.id, columns.map((c, i) => ({ ...c, position: i }))),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['board', board.projectId] })
    }
  })

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event
    
    if (over && active.id !== over.id) {
      setColumns((items) => {
        const oldIndex = items.findIndex(item => (item.tempId || item.id) === active.id)
        const newIndex = items.findIndex(item => (item.tempId || item.id) === over.id)
        return arrayMove(items, oldIndex, newIndex)
      })
    }
  }

  const updateColumn = (id: string | number, updater: Partial<BoardColumn>) => {
    setColumns(cols => cols.map(c => (c.tempId || c.id) === id ? { ...c, ...updater } : c))
  }

  const removeColumn = (id: string | number) => {
    setColumns(cols => cols.filter(c => (c.tempId || c.id) !== id))
  }

  return (
    <>
      <section className="page-heading">
        <div>
          <div className="eyebrow">Manage workflow</div>
          <h1>Board settings</h1>
          <p>Customize columns and workflow for this board.</p>
        </div>
        <button 
          className="button primary" 
          onClick={() => saveMutation.mutate()}
        >
          <Check size={16} /> Save changes
        </button>
      </section>
      
      <section className="surface settings-list">
        <div className="surface-head">
          <h2>Status columns</h2>
          <button 
            className="button secondary"
            onClick={() => setColumns([...columns, { id: 0, tempId: Date.now(), name: 'New Column', position: columns.length, tone: '#9ca3af' } as any])}
          >
            <Plus size={15} /> Add column
          </button>
        </div>
        
        <DndContext 
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleDragEnd}
        >
          <SortableContext 
            items={columns.map(c => c.tempId || c.id)}
            strategy={verticalListSortingStrategy}
          >
            {columns.map((column, index) => (
              <SortableColumnRow 
                key={column.tempId || column.id} 
                column={column}
                index={index}
                updateColumn={updateColumn}
                removeColumn={removeColumn}
              />
            ))}
          </SortableContext>
        </DndContext>
      </section>
    </>
  )
}
