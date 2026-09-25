export type Role = 'MEMBER' | 'MANAGER' | 'ADMIN'
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface User {
  id: number
  name: string
  email: string
  initials: string
  role: Role
  color: string
}

export interface Project {
  id: number
  boardId: number
  name: string
  description: string
  color: string
}

export interface BoardColumn {
  id: number
  name: string
  order: number
  tone: string
}

export interface Task {
  id: number
  projectId: number
  boardId: number
  title: string
  description: string
  status: string
  columnId: number
  assigneeId: number
  priority: Priority
  dueDate: string
  tags: string[]
  blocked: boolean
  blocker?: string
  updatedAt: string
}

export interface Ticket {
  id: number
  taskId: number
  title: string
  description: string
  statusColumnId: number
  priority: Priority
  assigneeId: number
  dueDate: string
  updatedAt: string
}

export interface Board {
  id: number
  projectId: number
  name: string
  description: string
  columns: BoardColumn[]
  tasks: Task[]
}

export interface Notification {
  id: number
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
