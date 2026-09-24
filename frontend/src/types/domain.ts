export type Role = 'MEMBER' | 'MANAGER' | 'ADMIN'
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface User {
  id: string | number
  name: string
  email: string
  initials: string
  role: Role
  color: string
}

export interface Project {
  id: string | number
  boardId: string | number
  name: string
  description: string
  color: string
}

export interface BoardColumn {
  id: string | number
  name: string
  order: number
  tone: string
}

export interface Task {
  id: string | number
  projectId?: string | number
  boardId: string | number
  title: string
  description: string
  status: string
  columnId?: string | number
  statusColumnId?: string | number
  assigneeId?: string | number | null
  priority: Priority
  dueDate: string
  tags: string[]
  blocked: boolean
  blocker?: string
  updatedAt: string
}

export interface Ticket {
  id: string | number
  taskId: string | number
  title: string
  description: string
  statusColumnId: string | number
  priority: Priority
  assigneeId: string | number
  dueDate: string
  updatedAt: string
}

export interface Board {
  id: string | number
  projectId: string | number
  name: string
  description: string
  columns: BoardColumn[]
  tasks: Task[]
}

export interface Notification {
  id: string | number
  title: string
  message: string
  type: 'deadline' | 'assignment' | 'update'
  read: boolean
  createdAt: string
}

export interface Session {
  token: string
  user: User
}
