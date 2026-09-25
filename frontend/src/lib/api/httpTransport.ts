import { apiClient } from './http/apiClient'
import type { ApiTransport, CreateTaskInput, CreateTicketInput, UpdateTaskInput, UpdateTicketInput } from './apiTransport'
import type { Notification, Session, Task, Ticket, User, Project, BoardColumn } from '../../types/domain'



const httpTransport: ApiTransport = {
  login: (email, password) => apiClient.post<Session>('/auth/login', { email, password }).then(({ data }) => data),
  getMe: () => apiClient.get<User>('/users/me').then(({ data }) => data),
  listUsers: () => apiClient.get<{ data: User[] }>('/users').then(({ data }) => data.data),
  createUser: (input) => apiClient.post<User>('/users', input).then(({ data }) => data),
  updateUser: (id, input) => apiClient.put<User>(`/users/${id}`, input).then(({ data }) => data),
  deleteUser: (id) => apiClient.delete(`/users/${id}`).then(() => undefined),
  listProjects: () => apiClient.get<{ data: Project[] }>('/boards').then(({ data }) => data.data),
  createProject: (input) => apiClient.post<Project>('/boards', input).then(({ data }) => data),
  updateProject: (id, input) => apiClient.put<Project>(`/boards/${id}`, input).then(({ data }) => data),
  deleteProject: (id) => apiClient.delete(`/boards/${id}`).then(() => undefined),
  getBoard: async (projectId, filters) => {
    const queryParams = filters ? new URLSearchParams(Object.entries(filters).filter(([_, v]) => v !== undefined && v !== '') as string[][]).toString() : '';
    const [boardResponse, tasksResponse] = await Promise.all([
      apiClient.get(`/boards/${projectId}`),
      apiClient.get(`/boards/${projectId}/tasks${queryParams ? `?${queryParams}` : ''}`)
    ]);
    const board = boardResponse.data;
    const tasks = tasksResponse.data.content || tasksResponse.data.items || (Array.isArray(tasksResponse.data) ? tasksResponse.data : []);
    return {
      ...board,
      projectId: String(projectId), // for compatibility
      tasks: tasks
    };
  },
  moveTask: (taskId, columnId) => apiClient.patch<Task>(`/tasks/${taskId}/status`, { 
    statusColumnId: columnId 
  }).then(({ data }) => data),
  updateColumns: (projectId, columns) => apiClient.put<BoardColumn[]>(`/boards/${projectId}/columns`, columns.map((c, i) => ({ name: c.name, position: i }))).then(({ data }) => data),
  getUsers: () => apiClient.get<{ data: User[] }>('/users').then(({ data }) => data.data),
  createTask: (input: CreateTaskInput) => apiClient.post<Task>('/tasks', input).then(({ data }) => data),
  updateTask: (taskId, input: UpdateTaskInput) => apiClient.put<Task>(`/tasks/${taskId}`, input).then(({ data }) => data),
  listTickets: (_taskId) => Promise.resolve([]), // Not in BE
  createTicket: (_input: CreateTicketInput) => Promise.resolve({} as Ticket), // Not in BE
  updateTicket: (_ticketId, _input: UpdateTicketInput) => Promise.resolve({} as Ticket), // Not in BE
  getNotifications: () => apiClient.get<{ items: Notification[] }>('/notifications').then(({ data }) => data.items),
  markNotificationRead: (id) => apiClient.patch(`/notifications/${id}/read`).then(() => undefined),
}

export { httpTransport }