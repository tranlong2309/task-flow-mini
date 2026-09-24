import type { Board, BoardColumn, Notification, Project, Session, Task, Ticket, User } from '../../types/domain'

export type CreateTaskInput = Pick<Task, 'title' | 'description' | 'assigneeId' | 'priority' | 'dueDate' | 'projectId' | 'boardId' | 'statusColumnId'>
export type UpdateTaskInput = Partial<Omit<CreateTaskInput, 'projectId' | 'boardId'>>
export type CreateTicketInput = Omit<Ticket, 'id' | 'updatedAt'>
export type UpdateTicketInput = Partial<Omit<Ticket, 'id' | 'taskId'>>

export interface ApiTransport {
  login(email: string, password: string): Promise<Session>
  getMe(token: string): Promise<User>
  listUsers(): Promise<User[]>
  createUser(input: { name: string, email: string, role: string }): Promise<User>
  updateUser(id: string | number, input: { name?: string, email?: string, role?: string }): Promise<User>
  deleteUser(id: string | number): Promise<void>
  listProjects(): Promise<Project[]>
  createProject(input: { name: string, description: string, teamId: number }): Promise<Project>
  updateProject(id: string | number, input: { name: string, description: string }): Promise<Project>
  deleteProject(id: string | number): Promise<void>
  getBoard(projectId: string | number): Promise<Board>
  moveTask(taskId: string | number, columnId: string | number): Promise<Task>
  updateColumns(projectId: string | number, columns: BoardColumn[]): Promise<BoardColumn[]>
  getUsers(): Promise<User[]>
  createTask(input: CreateTaskInput): Promise<Task>
  updateTask(taskId: string | number, input: UpdateTaskInput): Promise<Task>
  listTickets(taskId: string | number): Promise<Ticket[]>
  createTicket(input: CreateTicketInput): Promise<Ticket>
  updateTicket(ticketId: string | number, input: UpdateTicketInput): Promise<Ticket>
  getNotifications(): Promise<Notification[]>
  markNotificationRead(id: string | number): Promise<void>
}