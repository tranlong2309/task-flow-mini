import type { Board, Notification, Project, Session, Ticket, User } from '../types/domain'

export const users: User[] = [
  { id: 1, name: 'Linh Tran', email: 'linh@acme.test', initials: 'LT', role: 'MANAGER', color: '#5b5bd6' },
  { id: 2, name: 'Minh Nguyen', email: 'minh@acme.test', initials: 'MN', role: 'MEMBER', color: '#e07a5f' },
  { id: 3, name: 'An Pham', email: 'an@acme.test', initials: 'AP', role: 'MEMBER', color: '#2a9d8f' },
  { id: 4, name: 'Ha Le', email: 'ha@acme.test', initials: 'HL', role: 'ADMIN', color: '#e9a23b' },
]

export const projects: Project[] = [
  { id: 11, boardId: 101, name: 'Website Redesign', description: 'Q4 marketing website and launch campaign', color: '#5b56c7' },
  { id: 12, boardId: 102, name: 'Brand Campaign', description: 'Brand refresh and campaign rollout', color: '#e07a5f' },
]

export const seedBoard: Board = {
  id: 101,
  projectId: 11,
  name: 'Website Redesign',
  description: projects[0].description,
  columns: [
    { id: 1, name: 'Backlog', order: 1, tone: 'slate' },
    { id: 2, name: 'In progress', order: 2, tone: 'blue' },
    { id: 3, name: 'In review', order: 3, tone: 'amber' },
    { id: 4, name: 'Done', order: 4, tone: 'green' },
  ],
  tasks: [
    { id: 5001, projectId: 11, boardId: 101, title: 'Finalize homepage copy', description: 'Polish hero messaging and CTA variants.', status: 'IN_PROGRESS', columnId: 2, assigneeId: 2, priority: 'HIGH', dueDate: '2026-09-27', tags: ['Content', 'Website'], blocked: false, updatedAt: '2h ago' },
    { id: 5002, projectId: 11, boardId: 101, title: 'Design mobile navigation', description: 'Create responsive navigation states.', status: 'IN_PROGRESS', columnId: 2, assigneeId: 3, priority: 'MEDIUM', dueDate: '2026-09-29', tags: ['Design'], blocked: false, updatedAt: '4h ago' },
    { id: 5003, projectId: 11, boardId: 101, title: 'Set up analytics events', description: 'Track CTA and form conversion events.', status: 'TODO', columnId: 1, assigneeId: 4, priority: 'LOW', dueDate: '2026-10-02', tags: ['Analytics'], blocked: false, updatedAt: 'Yesterday' },
    { id: 5004, projectId: 11, boardId: 101, title: 'Review SEO metadata', description: 'Validate page titles, descriptions and sharing cards.', status: 'REVIEW', columnId: 3, assigneeId: 2, priority: 'MEDIUM', dueDate: '2026-09-25', tags: ['SEO'], blocked: true, blocker: 'Waiting for keyword list from client', updatedAt: '1d ago' },
    { id: 5005, projectId: 11, boardId: 101, title: 'QA contact form', description: 'Run validation and error state checks.', status: 'REVIEW', columnId: 3, assigneeId: 3, priority: 'HIGH', dueDate: '2026-09-26', tags: ['QA'], blocked: false, updatedAt: '3h ago' },
    { id: 5006, projectId: 11, boardId: 101, title: 'Publish launch checklist', description: 'Prepare final handoff checklist for launch.', status: 'DONE', columnId: 4, assigneeId: 1, priority: 'LOW', dueDate: '2026-09-23', tags: ['Launch'], blocked: false, updatedAt: '2d ago' },
  ],
}

export const seedBoards: Board[] = [
  seedBoard,
  {
    ...seedBoard,
    id: 102,
    projectId: 12,
    name: projects[1].name,
    description: projects[1].description,
    tasks: [
      { id: 5101, projectId: 12, boardId: 102, title: 'Approve campaign direction', description: 'Review the first campaign concept with stakeholders.', status: 'IN_PROGRESS', columnId: 2, assigneeId: 1, priority: 'HIGH', dueDate: '2026-10-01', tags: ['Campaign'], blocked: false, updatedAt: 'Today' },
      { id: 5102, projectId: 12, boardId: 102, title: 'Prepare launch assets', description: 'Create the launch asset checklist for the campaign.', status: 'TODO', columnId: 1, assigneeId: 3, priority: 'MEDIUM', dueDate: '2026-10-05', tags: ['Launch'], blocked: false, updatedAt: 'Yesterday' },
    ],
  },
]

export const seedTickets: Ticket[] = [
  { id: 7001, taskId: 5001, title: 'Hero CTA copy variant', description: 'Create three CTA options for the homepage hero.', statusColumnId: 2, priority: 'HIGH', assigneeId: 2, dueDate: '2026-09-26', updatedAt: 'Today' },
  { id: 7002, taskId: 5001, title: 'Proofread value proposition', description: 'Review grammar and brand voice.', statusColumnId: 3, priority: 'MEDIUM', assigneeId: 2, dueDate: '2026-09-27', updatedAt: 'Yesterday' },
  { id: 7003, taskId: 5004, title: 'Collect SEO keywords', description: 'Confirm keyword list with the client.', statusColumnId: 1, priority: 'HIGH', assigneeId: 1, dueDate: '2026-09-24', updatedAt: '2h ago' },
]

export const notifications: Notification[] = [
  { id: 1, title: 'Deadline approaching', message: 'Review SEO metadata is due tomorrow.', type: 'deadline', read: false, createdAt: '12 min ago' },
  { id: 2, title: 'Task assigned to you', message: 'Minh assigned “Finalize homepage copy”.', type: 'assignment', read: false, createdAt: '2 hours ago' },
  { id: 3, title: 'Task moved to review', message: 'QA contact form is ready for review.', type: 'update', read: true, createdAt: '3 hours ago' },
]

export const demoSessions: Record<string, Session> = Object.fromEntries(users.map((user) => [
  user.email,
  { token: `mock-token-${user.id}`, user },
]))
