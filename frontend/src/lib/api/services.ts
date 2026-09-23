import { env } from '../../config/env'
import { mockTransport } from './mockTransport'
import { httpTransport } from './httpTransport'
import type { Board, Notification, Project, Session, Task, Ticket, User } from '../../types/domain'
import type { ApiTransport, CreateTaskInput, CreateTicketInput, UpdateTaskInput, UpdateTicketInput } from './apiTransport'

const transport: ApiTransport = env.apiMode === 'mock' ? mockTransport : httpTransport

export const authService = {
  login: (email: string, password: string): Promise<Session> => transport.login(email, password),
  me: (token: string): Promise<User> => transport.getMe(token),
}
export const projectService = {
  list: (): Promise<Project[]> => transport.listProjects(),
}
export const boardService = {
  get: (projectId = 11): Promise<Board> => transport.getBoard(projectId),
  moveTask: (taskId: number, columnId: number): Promise<Task> => transport.moveTask(taskId, columnId),
  updateColumns: (columns: Board['columns']): Promise<Board> => transport.updateColumns(columns),
}
export const taskService = {
  listUsers: (): Promise<User[]> => transport.getUsers(),
  create: (input: CreateTaskInput): Promise<Task> => transport.createTask(input),
  update: (taskId: number, input: UpdateTaskInput): Promise<Task> => transport.updateTask(taskId, input),
}
export const ticketService = {
  list: (taskId: number): Promise<Ticket[]> => transport.listTickets(taskId),
  create: (input: CreateTicketInput): Promise<Ticket> => transport.createTicket(input),
  update: (ticketId: number, input: UpdateTicketInput): Promise<Ticket> => transport.updateTicket(ticketId, input),
}
export const notificationService = {
  list: (): Promise<Notification[]> => transport.getNotifications(),
  markRead: (id: number): Promise<void> => transport.markNotificationRead(id),
}
