import { demoSessions, notifications, projects, seedBoards, seedTickets, users } from '../../mocks/data'
import type { ApiTransport, CreateTaskInput, CreateTicketInput, UpdateTaskInput, UpdateTicketInput } from './apiTransport'
import type { Board, BoardColumn, Notification, Project, Session, Task, Ticket, User } from '../../types/domain'

let boards = structuredClone(seedBoards)
let tickets = structuredClone(seedTickets)
let inbox = structuredClone(notifications)
const wait = (ms = 220) => new Promise((resolve) => setTimeout(resolve, ms))
const copy = <T>(value: T): T => structuredClone(value)
const fail = (status: number, detail: string): never => { throw new Error(`${status}: ${detail}`) }

export const mockTransport: ApiTransport = {
  async login(email: string, password: string): Promise<Session> {
    await wait(300)
    if (password !== 'password' || !demoSessions[email]) fail(401, 'Invalid email or password')
    return copy(demoSessions[email])
  },
  async getMe(token: string): Promise<User> {
    await wait()
    const session = Object.values(demoSessions).find((item) => item.token === token)
    if (!session) throw new Error('401: Session expired')
    return copy(session.user)
  },
  async listProjects(): Promise<Project[]> { await wait(); return copy(projects) },
  async getBoard(projectId: number): Promise<Board> {
    await wait()
    const board = boards.find((item) => item.projectId === projectId)
    if (!board) throw new Error('404: Project board not found')
    return copy(board)
  },
  async getUsers(): Promise<User[]> { await wait(100); return copy(users) },
  async moveTask(taskId: number, columnId: number): Promise<Task> {
    await wait(350)
    const board = boards.find((item) => item.tasks.some((task) => task.id === taskId))
    const task = board?.tasks.find((item) => item.id === taskId)
    if (!task) throw new Error('404: Task not found')
    task.columnId = columnId
    task.status = board?.columns.find((column) => column.id === columnId)?.name.toUpperCase().replace(' ', '_') ?? task.status
    task.updatedAt = 'Just now'
    return copy(task)
  },
  async createTask(input: CreateTaskInput): Promise<Task> {
    await wait(300)
    if (!input.title.trim()) fail(422, 'Task title is required')
    const board = boards.find((item) => item.projectId === input.projectId)
    if (!board) throw new Error('404: Project board not found')
    const task: Task = { ...input, id: Date.now(), boardId: board.id, columnId: 1, status: 'TODO', tags: ['New'], blocked: false, updatedAt: 'Just now' }
    board.tasks.unshift(task)
    return copy(task)
  },
  async updateTask(taskId: number, input: UpdateTaskInput): Promise<Task> {
    await wait(300)
    const board = boards.find((item) => item.tasks.some((task) => task.id === taskId))
    const task = board?.tasks.find((item) => item.id === taskId)
    if (!task) throw new Error('404: Task not found')
    Object.assign(task, input, { updatedAt: 'Just now' })
    return copy(task)
  },
  async listTickets(taskId: number): Promise<Ticket[]> { await wait(180); return copy(tickets.filter((ticket) => ticket.taskId === taskId)) },
  async createTicket(input: CreateTicketInput): Promise<Ticket> {
    await wait(280)
    if (!input.title.trim()) fail(422, 'Ticket title is required')
    const ticket = { ...input, id: Date.now(), updatedAt: 'Just now' }
    tickets.unshift(ticket)
    return copy(ticket)
  },
  async updateTicket(ticketId: number, input: UpdateTicketInput): Promise<Ticket> {
    await wait(280)
    const ticket = tickets.find((item) => item.id === ticketId)
    if (!ticket) throw new Error('404: Ticket not found')
    Object.assign(ticket, input, { updatedAt: 'Just now' })
    return copy(ticket)
  },
  async getNotifications(): Promise<Notification[]> { await wait(120); return copy(inbox) },
  async markNotificationRead(id: number) { await wait(100); inbox = inbox.map((item) => item.id === id ? { ...item, read: true } : item) },
  async updateColumns(columns: BoardColumn[]) {
    await wait(260)
    const board = boards[0]
    board.columns = columns.map((column, index) => ({ ...column, order: index + 1 }))
    return copy(board)
  },
}
