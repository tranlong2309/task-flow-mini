import { env } from '../../config/env'
import { mockTransport } from './mockTransport'
import { httpTransport } from './httpTransport'
import type { Board, BoardColumn, Notification, Project, Session, Task, Ticket, User } from '../../types/domain'
import type { ApiTransport, CreateTaskInput, CreateTicketInput, UpdateTaskInput, UpdateTicketInput } from './apiTransport'

const transport: ApiTransport = env.apiMode === 'mock' ? mockTransport as unknown as ApiTransport : httpTransport

export const authService = {
  login: (email: string, password: string): Promise<Session> => transport.login(email, password),
  me: (token: string): Promise<User> => transport.getMe(token),
}
export const userService = {
  list: (): Promise<User[]> => transport.listUsers(),
  create: (input: { name: string, email: string, role: string }): Promise<User> => transport.createUser(input),
  update: (id: string | number, input: { name?: string, email?: string, role?: string }): Promise<User> => transport.updateUser(id, input),
  delete: (id: string | number): Promise<void> => transport.deleteUser(id),
}
export const projectService = {
  list: (): Promise<Project[]> => transport.listProjects(),
  create: (input: { name: string, description: string, teamId: number }): Promise<Project> => transport.createProject(input),
  update: (id: string | number, input: { name: string, description: string }): Promise<Project> => transport.updateProject(id, input),
  delete: (id: string | number): Promise<void> => transport.deleteProject(id),
}
export const boardService = {
  get: (projectId: string | number, filters?: import('./apiTransport').TaskFilters): Promise<Board> => transport.getBoard(projectId, filters),
  moveTask: (taskId: string | number, columnId: string | number): Promise<Task> => transport.moveTask(taskId, columnId),
  updateColumns: (projectId: string | number, columns: Board['columns']): Promise<BoardColumn[]> => transport.updateColumns(projectId, columns),
}
export const taskService = {
  listUsers: (): Promise<User[]> => transport.getUsers(),
  create: (input: CreateTaskInput): Promise<Task> => transport.createTask(input),
  update: (taskId: string | number, input: UpdateTaskInput): Promise<Task> => transport.updateTask(taskId, input),
}
export const ticketService = {
  list: (taskId: string | number): Promise<Ticket[]> => transport.listTickets(taskId),
  create: (input: CreateTicketInput): Promise<Ticket> => transport.createTicket(input),
  update: (ticketId: string | number, input: UpdateTicketInput): Promise<Ticket> => transport.updateTicket(ticketId, input),
}
export const notificationService = {
  list: (): Promise<Notification[]> => transport.getNotifications(),
  markRead: (id: string | number): Promise<void> => transport.markNotificationRead(id),
}
