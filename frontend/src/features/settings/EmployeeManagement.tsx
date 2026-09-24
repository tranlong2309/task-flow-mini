import { useState } from 'react'
import { Users, Plus, MoreHorizontal, Pencil, Trash, X } from 'lucide-react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import type { User } from '../../types/domain'
import { userService } from '../../lib/api/services'

export function EmployeeManagement({ users }: { users: User[] }) {
  const queryClient = useQueryClient()
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingId, setEditingId] = useState<string | number | null>(null)
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [role, setRole] = useState('MEMBER')

  const openModal = (user?: User) => {
    if (user) {
      setEditingId(user.id)
      setName(user.name)
      setEmail(user.email)
      setRole(user.role)
    } else {
      setEditingId('new')
      setName('')
      setEmail('')
      setRole('MEMBER')
    }
    setIsModalOpen(true)
  }

  const closeModal = () => {
    setIsModalOpen(false)
    setEditingId(null)
  }

  const createMutation = useMutation({
    mutationFn: () => userService.create({ name, email, role }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      closeModal()
    }
  })

  const updateMutation = useMutation({
    mutationFn: () => 
      userService.update(editingId as string | number, { name, email, role }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      closeModal()
    }
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string | number) => userService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
    }
  })

  return (
    <>
      <section className="page-heading">
        <div>
          <div className="eyebrow">Manage workflow</div>
          <h1>Employees</h1>
          <p>Manage members, roles, and permissions across the workspace.</p>
        </div>
        <button className="button primary" onClick={() => openModal()}>
          <Plus size={16} /> Invite member
        </button>
      </section>
      
      {isModalOpen && (
        <div className="modal-backdrop">
          <form 
            className="modal" 
            onSubmit={(e) => {
              e.preventDefault()
              if (!name.trim() || !email.trim()) return
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
                <h2>{editingId === 'new' ? 'Invite New Member' : 'Edit Member'}</h2>
              </div>
              <button type="button" className="icon-button" onClick={closeModal}>
                <X size={18} />
              </button>
            </div>
            
            <label>
              Full Name
              <input 
                autoFocus 
                value={name} 
                onChange={(e) => setName(e.target.value)} 
                placeholder="e.g. Jane Doe" 
                required 
              />
            </label>
            
            <label style={{ marginTop: '16px' }}>
              Email Address
              <input 
                type="email"
                value={email} 
                onChange={(e) => setEmail(e.target.value)} 
                placeholder="jane@example.com" 
                required
              />
            </label>

            <label style={{ marginTop: '16px' }}>
              Workspace Role
              <select value={role} onChange={(e) => setRole(e.target.value)}>
                <option value="MEMBER">Member</option>
                <option value="SUPERVISOR">Supervisor</option>
                <option value="ADMIN">Admin</option>
              </select>
            </label>
            
            <div className="modal-actions">
              <button type="button" className="button secondary" onClick={closeModal}>Cancel</button>
              <button type="submit" className="button primary">
                {editingId === 'new' ? 'Send Invitation' : 'Save Changes'}
              </button>
            </div>
          </form>
        </div>
      )}

      <section className="surface settings-list">
        <div className="surface-head">
          <h2>Team Members ({users.length})</h2>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Member Name</th>
              <th>Email</th>
              <th>Role</th>
              <th className="align-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>
                  <div className="flex-row">
                    <div className="avatar" style={{ background: 'var(--blue)' }}>{user.name.charAt(0)}</div>
                    <strong>{user.name}</strong>
                  </div>
                </td>
                <td>
                  {user.email}
                </td>
                <td>
                  <span className="badge">{user.role}</span>
                </td>
                <td className="align-right">
                  <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
                    <button className="plain-button" onClick={() => openModal(user)}><Pencil size={17} /></button>
                    <button className="plain-button" onClick={() => { if(confirm('Are you sure?')) deleteMutation.mutate(user.id) }}><Trash size={17} color="red" /></button>
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
