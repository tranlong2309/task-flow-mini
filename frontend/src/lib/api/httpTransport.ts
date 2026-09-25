import { apiClient } from './http/apiClient'
import type { ApiTransport, CreateTaskInput, CreateTicketInput, UpdateTaskInput, UpdateTicketInput } from './apiTransport'
import type { Board, Notification, Project, Session, Task, Ticket, User } from '../../types/domain'

const httpTransport: ApiTransport = {
  login: (email, password) => apiClient.post<Session>('/auth/login', { email, password }).then(({ data }) => data),
  getMe: () => apiClient.get<User>('/users/me').then(({ data }) => data),
  listProjects: () => apiClient.get<{ data: Project[] }>('/projects').then(({ data }) => data.data),
  getBoard: (projectId) => apiClient.get<Board>(`/projects/${projectId}/board`).then(({ data }) => data),
  moveTask: (taskId, columnId) => apiClient.patch<Task>(`/tasks/${taskId}/move`, { targetColumnId: columnId }).then(({ data }) => data),
  updateColumns: (columns) => apiClient.put<Board>('/boards/101', { statusColumns: columns }).then(({ data }) => data),
  getUsers: () => apiClient.get<{ data: User[] }>('/users').then(({ data }) => data.data),
  createTask: (input: CreateTaskInput) => apiClient.post<Task>('/tasks', input).then(({ data }) => data),
  updateTask: (taskId, input: UpdateTaskInput) => apiClient.put<Task>(`/tasks/${taskId}`, input).then(({ data }) => data),
  listTickets: (taskId) => apiClient.get<{ data: Ticket[] }>(`/tasks/${taskId}/tickets`).then(({ data }) => data.data),
  createTicket: (input: CreateTicketInput) => apiClient.post<Ticket>('/tickets', input).then(({ data }) => data),
  updateTicket: (ticketId, input: UpdateTicketInput) => apiClient.put<Ticket>(`/tickets/${ticketId}`, input).then(({ data }) => data),
  getNotifications: () => apiClient.get<{ items: Notification[] }>('/notifications').then(({ data }) => data.items),
  markNotificationRead: (id) => apiClient.patch(`/notifications/${id}/read`).then(() => undefined),
}

export { httpTransport }