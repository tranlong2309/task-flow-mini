import { useState } from 'react'
import { Plus, Pencil, Trash, X } from 'lucide-react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import type { Project } from '../../types/domain'
import { projectService } from '../../lib/api/services'

export function ProjectManagement({ projects }: { projects: Project[] }) {
  const queryClient = useQueryClient()
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingId, setEditingId] = useState<string | number | null>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  const openModal = (project?: Project) => {
    if (project) {
      setEditingId(project.id)
      setName(project.name)
      setDescription(project.description)
    } else {
      setEditingId('new')
      setName('')
      setDescription('')
    }
    setIsModalOpen(true)
  }

  const closeModal = () => {
    setIsModalOpen(false)
    setEditingId(null)
  }

  const createMutation = useMutation({
    mutationFn: () => projectService.create({ name, description, teamId: 1 }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] })
      closeModal()
    }
  })

  const updateMutation = useMutation({
    mutationFn: () => 
      projectService.update(editingId as string | number, { name, description }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] })
      closeModal()
    }
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string | number) => projectService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] })
    }
  })

  return (
    <>
      <section className="page-heading">
        <div>
          <div className="eyebrow">Manage workflow</div>
          <h1>Projects</h1>
          <p>Manage all boards and projects in your workspace.</p>
        </div>
        <button className="button primary" onClick={() => openModal()}>
          <Plus size={16} /> New project
        </button>
      </section>
      
      {isModalOpen && (
        <div className="modal-backdrop">
          <form 
            className="modal" 
            onSubmit={(e) => {
              e.preventDefault()
              if (!name.trim()) return
              if (editingId === 'new') {
                createMutation.mutate()
              } else {
                updateMutation.mutate()
              }
            }}
          >
            <div className="modal-head">
              <div>
                <span className="eyebrow">Workspace</span>
                <h2>{editingId === 'new' ? 'Create new project' : 'Edit project'}</h2>
              </div>
              <button type="button" className="icon-button" onClick={closeModal}>
                <X size={18} />
              </button>
            </div>
            
            <label>
              Project Name
              <input 
                autoFocus 
                value={name} 
                onChange={(e) => setName(e.target.value)} 
                placeholder="e.g. Design System V2" 
                required 
              />
            </label>
            
            <label style={{ marginTop: '16px' }}>
              Description
              <input 
                value={description} 
                onChange={(e) => setDescription(e.target.value)} 
                placeholder="Brief description of the project" 
              />
            </label>
            
            <div className="modal-actions">
              <button type="button" className="button secondary" onClick={closeModal}>Cancel</button>
              <button type="submit" className="button primary">
                {editingId === 'new' ? 'Create Project' : 'Save Changes'}
              </button>
            </div>
          </form>
        </div>
      )}

      <section className="surface settings-list">
        <div className="surface-head">
          <h2>All projects ({projects.length})</h2>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Project Name</th>
              <th>Status</th>
              <th>Created Date</th>
              <th className="align-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {projects.map((project) => (
              <tr key={project.id}>
                <td>
                  <div className="flex-row">
                    <span className="project-color" />
                    <strong>{project.name}</strong>
                  </div>
                </td>
                <td><span className="badge success">Active</span></td>
                <td>Just now</td>
                <td className="align-right">
                  <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
                    <button className="plain-button" onClick={() => openModal(project)}><Pencil size={17} /></button>
                    <button className="plain-button" onClick={() => { if(confirm('Are you sure?')) deleteMutation.mutate(project.id) }}><Trash size={17} color="red" /></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </>
  )
}
