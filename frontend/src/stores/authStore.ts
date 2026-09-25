import { create } from 'zustand'
import type { Role, Session, Task, User } from '../types/domain'

interface AuthState {
  token: string | null
  user: User | null
  hydrate: () => void
  setSession: (session: Session) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  user: null,
  hydrate: () => {
    const token = localStorage.getItem('taskflow.token')
    const rawUser = localStorage.getItem('taskflow.user')
    set({ token, user: rawUser ? JSON.parse(rawUser) as User : null })
  },
  setSession: (session) => {
    localStorage.setItem('taskflow.token', session.token)
    localStorage.setItem('taskflow.user', JSON.stringify(session.user))
    set(session)
  },
  logout: () => {
    localStorage.removeItem('taskflow.token')
    localStorage.removeItem('taskflow.user')
    set({ token: null, user: null })
  },
}))

export const can = (role: Role | undefined, permission: 'manageBoard' | 'editTask' | 'manageUsers' | 'createTask' | 'editTicket') => {
  if (!role) return false
  if (role === 'ADMIN') return true
  if (permission === 'manageUsers' || permission === 'manageBoard') return false
  if (permission === 'editTask') return role === 'MANAGER'
  return permission === 'createTask' || permission === 'editTicket'
}

export const canEditTask = (user: User | null, task: Task) => {
  if (!user) return false;
  if (user.role === 'ADMIN' || user.role === 'MANAGER') return true;
  
  const userIdStr = String(user.id);
  if (task.assigneeIds && task.assigneeIds.some(id => String(id) === userIdStr)) return true;
  if (task.assigneeId && String(task.assigneeId) === userIdStr) return true;
  
  return false;
}
