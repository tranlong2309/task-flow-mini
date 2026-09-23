import type { Board, BoardColumn, Notification, Project, Session, Task, Ticket, User } from '../../types/domain'

export type CreateTaskInput = Pick<Task, 'title' | 'description' | 'assigneeId' | 'priority' | 'dueDate' | 'projectId'>
export type UpdateTaskInput = Partial<Omit<CreateTaskInput, 'projectId'>>
export type CreateTicketInput = Omit<Ticket, 'id' | 'updatedAt'>
export type UpdateTicketInput = Partial<Omit<Ticket, 'id' | 'taskId'>>

export interface ApiTransport {
  login(email: string, password: string): Promise<Session>
  getMe(token: string): Promise<User>
  listProjects(): Promise<Project[]>
  getBoard(projectId: number): Promise<Board>
  moveTask(taskId: number, columnId: number): Promise<Task>
  updateColumns(columns: BoardColumn[]): Promise<Board>
  getUsers(): Promise<User[]>
  createTask(input: CreateTaskInput): Promise<Task>
  updateTask(taskId: number, input: UpdateTaskInput): Promise<Task>
  listTickets(taskId: number): Promise<Ticket[]>
  createTicket(input: CreateTicketInput): Promise<Ticket>
  updateTicket(ticketId: number, input: UpdateTicketInput): Promise<Ticket>
  getNotifications(): Promise<Notification[]>
  markNotificationRead(id: number): Promise<void>
}