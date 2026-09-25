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

export interface Subtask {
  id: string
  title: string
  isCompleted: boolean
  createdAt: string
}

export interface Comment {
  id: string
  userId: string | number
  content: string
  createdAt: string
}

export interface Attachment {
  id: string
  fileName: string
  fileUrl: string
  uploadedBy: string | number
  createdAt: string
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
  assigneeIds?: (string | number)[]
  assignedDate?: string | null
  startDate?: string | null
  subtasks?: Subtask[]
  comments?: Comment[]
  attachments?: Attachment[]
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

// --- SPEC-DRIVEN TYPES (Aligned with docs/api-spec.yaml & docs/domain-model.md) ---

export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
  trackingId?: string;
  violations?: { field: string; message: string }[];
}

export interface WorkOrderSummary {
  id: string;
  title: string;
  statusColumnId: number;
  statusColumnName: string;
  priority: Priority;
  dueDate: string; // ISO-8601
  assigneeId?: number;
  isBlocked: boolean;
  isOverdue: boolean;
  createdAt: string;
}

export interface PagedWorkOrderResponse {
  items: WorkOrderSummary[];
  totalCount: number;
  page: number;
  pageSize: number;
  hasNext: boolean;
}

export interface WorkOrderResponse {
  id: string;
  boardId: string;
  title: string;
  description: string;
  statusColumnId: number;
  statusColumnName: string;
  priority: Priority;
  dueDate: string;
  isOverdue: boolean;
  assigneeId?: number;
  isBlocked: boolean;
  blockedReason?: string;
  blockedAt?: string;
  completedAt?: string;
  createdBy: number;
  createdAt: string;
  updatedAt: string;
}
