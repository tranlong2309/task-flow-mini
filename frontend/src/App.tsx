import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { QueryClient, QueryClientProvider, useMutation, useQuery } from '@tanstack/react-query'
import { DndContext, DragOverlay, PointerSensor, useDraggable, useDroppable, useSensor, useSensors, type DragEndEvent } from '@dnd-kit/core'
import { Bell, CalendarDays, Check, ChevronDown, CircleHelp, Download, Filter, LayoutDashboard, MoreHorizontal, Plus, Search, Settings, Sparkles, Target, Users, X } from 'lucide-react'
import { authService, boardService, notificationService, projectService, taskService, ticketService } from './lib/api/services'
import { useAppStore } from './stores/useAppStore'
import { can, useAuthStore } from './stores/authStore'
import type { Board, BoardColumn, Task, Ticket, User } from './types/domain'
import './App.css'

const queryClient = new QueryClient({ defaultOptions: { queries: { staleTime: 30_000 } } })

function Avatar({ user, small = false }: { user?: User; small?: boolean }) {
  if (!user) return <span className={`avatar avatar-muted ${small ? 'small' : ''}`}>?</span>
  return <span className={`avatar ${small ? 'small' : ''}`} style={{ background: user.color }}>{user.initials}</span>
}

function LoginScreen({ email, password, error, loading, onEmail, onPassword, onSubmit }: { email: string; password: string; error: string; loading: boolean; onEmail: (value: string) => void; onPassword: (value: string) => void; onSubmit: (event: FormEvent<HTMLFormElement>) => void }) {
  return <div className="login-screen"><div className="login-card"><div className="brand login-brand"><div className="brand-mark"><Sparkles size={16} /></div><span>taskflow</span></div><span className="eyebrow">Welcome back</span><h1>Sign in to your workspace</h1><p>Use one of the demo accounts to explore role-based access.</p><form onSubmit={onSubmit}><label>Email<input type="email" value={email} onChange={(event) => onEmail(event.target.value)} /></label><label>Password<input type="password" value={password} onChange={(event) => onPassword(event.target.value)} /></label>{error && <div className="form-error">{error}</div>}<button className="button primary full" disabled={loading}>{loading ? 'Signing in...' : 'Sign in'}</button></form><div className="demo-hint"><strong>Demo accounts</strong><small>linh@acme.test · Manager</small><small>minh@acme.test · Member</small><small>ha@acme.test · Admin</small><small>Password: password</small></div></div></div>
}

function AppContent() {
  const { user, token, hydrate, setSession, logout } = useAuthStore()
  const [loginEmail, setLoginEmail] = useState('linh@acme.test')
  const [loginPassword, setLoginPassword] = useState('password')
  const [loginError, setLoginError] = useState('')
  const [loggingIn, setLoggingIn] = useState(false)
  useEffect(() => hydrate(), [hydrate])
  if (!token || !user) return <LoginScreen email={loginEmail} password={loginPassword} error={loginError} loading={loggingIn} onEmail={setLoginEmail} onPassword={setLoginPassword} onSubmit={async (event) => { event.preventDefault(); setLoggingIn(true); setLoginError(''); try { setSession(await authService.login(loginEmail, loginPassword)) } catch { setLoginError('Invalid email or password. Use password for the demo.') } finally { setLoggingIn(false) } }} />
  return <AuthenticatedApp user={user} logout={logout} />
}

function AuthenticatedApp({ user, logout }: { user: User; logout: () => void }) {
  const { view, setView, search, setSearch, priority, setPriority, selectedTaskId, setSelectedTaskId, selectedProjectId, setSelectedProjectId } = useAppStore()
  const boardQuery = useQuery({ queryKey: ['board', selectedProjectId], queryFn: () => boardService.get(selectedProjectId) })
  const usersQuery = useQuery({ queryKey: ['users'], queryFn: taskService.listUsers })
  const projectQuery = useQuery({ queryKey: ['projects'], queryFn: projectService.list })
  const notificationQuery = useQuery({ queryKey: ['notifications'], queryFn: notificationService.list })
  const [activeId, setActiveId] = useState<number | null>(null)
  const [showNotifications, setShowNotifications] = useState(false)
  const [showCreate, setShowCreate] = useState(false)
  const sensors = useSensors(useSensor(PointerSensor))
  const board = boardQuery.data
  const projects = projectQuery.data ?? []
  const users = usersQuery.data ?? []
  const unread = notificationQuery.data?.filter((item) => !item.read).length ?? 0
  const moveMutation = useMutation({
        mutationFn: ({ taskId, columnId }: { taskId: number; columnId: number }) => boardService.moveTask(taskId, columnId),
    onMutate: async ({ taskId, columnId }) => {
      await queryClient.cancelQueries({ queryKey: ['board', selectedProjectId] })
      const previous = queryClient.getQueryData<Board>(['board', selectedProjectId])
      queryClient.setQueryData<Board>(['board', selectedProjectId], (current) => current && ({ ...current, tasks: current.tasks.map((task) => task.id === taskId ? { ...task, columnId, updatedAt: 'Just now' } : task) }))
      return { previous }
    },
    onError: (_error, _variables, context) => queryClient.setQueryData(['board', selectedProjectId], context?.previous),
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['board', selectedProjectId] }),
  })

  const filteredTasks = useMemo(() => board?.tasks.filter((task) => {
    const matchesSearch = !search || `${task.title} ${task.description}`.toLowerCase().includes(search.toLowerCase())
    const matchesPriority = priority === 'all' || task.priority === priority
    return matchesSearch && matchesPriority
  }) ?? [], [board?.tasks, priority, search])

  if (boardQuery.isLoading) return <div className="loading-screen"><Sparkles size={20} /> Loading your workspace...</div>
  if (boardQuery.isError || !board) return <div className="loading-screen error">Could not load the board. Please try again.</div>

  const selectedTask = board.tasks.find((task) => task.id === selectedTaskId)
  const activeTask = board.tasks.find((task) => task.id === activeId)

  function handleDragEnd(event: DragEndEvent) {
    setActiveId(null)
    const { active, over } = event
    if (!over || active.id === over.id) return
    const column = board?.columns.find((item) => item.id === Number(over.id))
    if (column) moveMutation.mutate({ taskId: Number(active.id), columnId: column.id })
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand"><div className="brand-mark"><Sparkles size={16} /></div><span>taskflow</span></div>
        <div className="workspace-switcher"><div className="workspace-icon">W</div><div><strong>Workspace</strong><small>Acme Studio</small></div><ChevronDown size={15} /></div>
        <label className="project-switcher"><span className="project-color" /><div><small>Project</small><select value={selectedProjectId} onChange={(event) => setSelectedProjectId(Number(event.target.value))}>{projects.map((project) => <option value={project.id} key={project.id}>{project.name}</option>)}</select></div></label>
        <nav className="nav">
          <p className="nav-label">Workspace</p>
          <button className={view === 'board' ? 'nav-item active' : 'nav-item'} onClick={() => setView('board')}><LayoutDashboard size={18} /> My board <span className="nav-count">6</span></button>
          <button className={view === 'workload' ? 'nav-item active' : 'nav-item'} onClick={() => setView('workload')}><Users size={18} /> Workload</button>
          <button className={view === 'reports' ? 'nav-item active' : 'nav-item'} onClick={() => setView('reports')}><Target size={18} /> Reports</button>
          <p className="nav-label">Manage</p>
          {can(user.role, 'manageBoard') && <button className={view === 'settings' ? 'nav-item active' : 'nav-item'} onClick={() => setView('settings')}><Settings size={18} /> Board settings</button>}
        </nav>
        <div className="sidebar-bottom"><div className="help-card"><CircleHelp size={17} /><div><strong>Need a hand?</strong><small>Visit help center</small></div></div><button className="profile" onClick={logout}><Avatar user={user} /><div><strong>{user.name}</strong><small>{user.role}</small></div><span className="logout-label">Log out</span></button></div>
      </aside>
      <main className="main">
        <header className="topbar"><div className="breadcrumbs"><span>Workspace</span><span>/</span><strong>Website Redesign</strong></div><div className="top-actions"><label className="search"><Search size={17} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search tasks..." /><kbd>⌘ K</kbd></label><button className="icon-button" onClick={() => setShowNotifications(!showNotifications)} aria-label="Notifications"><Bell size={19} />{unread > 0 && <i>{unread}</i>}</button><Avatar user={users[0]} /></div></header>
        {showNotifications && <NotificationPanel items={notificationQuery.data ?? []} onRead={async (id) => { await notificationService.markRead(id); queryClient.invalidateQueries({ queryKey: ['notifications'] }) }} onClose={() => setShowNotifications(false)} />}
        {view === 'board' && <><section className="page-heading"><div><div className="eyebrow">Project board <span className="live-dot" /> Live</div><h1>{board.name}</h1><p>{board.description}</p></div><div className="heading-actions"><button className="button secondary"><Filter size={16} /> Filter</button><button className="button primary" onClick={() => setShowCreate(true)}><Plus size={17} /> New task</button></div></section><div className="board-toolbar"><div className="filter-chips"><span>Showing <strong>{filteredTasks.length} tasks</strong></span><button className={priority === 'all' ? 'chip selected' : 'chip'} onClick={() => setPriority('all')}>All</button>{(['HIGH', 'MEDIUM', 'LOW'] as const).map((item) => <button key={item} className={priority === item ? 'chip selected' : 'chip'} onClick={() => setPriority(item)}>{item[0] + item.slice(1).toLowerCase()}</button>)}</div><div className="board-meta"><span><span className="green-dot" /> Synced just now</span><button className="icon-button"><MoreHorizontal size={18} /></button></div></div><DndContext sensors={sensors} onDragStart={(event) => setActiveId(Number(event.active.id))} onDragEnd={handleDragEnd} onDragCancel={() => setActiveId(null)}><div className="kanban">{board.columns.map((column) => <KanbanColumn key={column.id} column={column} tasks={filteredTasks.filter((task) => task.columnId === column.id)} users={users} onTaskClick={setSelectedTaskId} />)}</div><DragOverlay>{activeTask ? <TaskCard task={activeTask} user={users.find((item) => item.id === activeTask.assigneeId)} overlay /> : null}</DragOverlay></DndContext></>}
        {view === 'workload' && <Workload board={board} users={users} />}
        {view === 'reports' && <Reports board={board} />}
        {view === 'settings' && <BoardSettings board={board} />}
      </main>
      {selectedTask && <TaskDrawer task={selectedTask} user={users.find((item) => item.id === selectedTask.assigneeId)} column={board.columns.find((item) => item.id === selectedTask.columnId)} canEdit={can(user.role, 'editTask')} onClose={() => setSelectedTaskId(null)} />}
      {showCreate && <CreateTask users={users} onClose={() => setShowCreate(false)} onCreated={() => { setShowCreate(false); queryClient.invalidateQueries({ queryKey: ['board', 101] }) }} />}
    </div>
  )
}

function KanbanColumn({ column, tasks, users, onTaskClick }: { column: BoardColumn; tasks: Task[]; users: User[]; onTaskClick: (id: number) => void }) {
  const { setNodeRef, isOver } = useDroppable({ id: column.id })
  return <section ref={setNodeRef} className={`kanban-column ${isOver ? 'drop-active' : ''}`}><div className="column-head"><div><span className={`column-dot ${column.tone}`} /><strong>{column.name}</strong><span className="task-count">{tasks.length}</span></div><button className="plain-button"><MoreHorizontal size={17} /></button></div><div className="column-body">{tasks.map((task) => <DraggableTask key={task.id} task={task} user={users.find((item) => item.id === task.assigneeId)} onClick={() => onTaskClick(task.id)} />)}{tasks.length === 0 && <div className="column-empty">Drop tasks here</div>}<button className="add-task" onClick={() => onTaskClick(0)}><Plus size={15} /> Add task</button></div></section>
}

function DraggableTask({ task, user, onClick }: { task: Task; user?: User; onClick: () => void }) {
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({ id: task.id })
  return <article ref={setNodeRef} style={{ transform: transform ? `translate3d(${transform.x}px, ${transform.y}px, 0)` : undefined }} className={`task-card ${isDragging ? 'dragging' : ''}`} {...listeners} {...attributes} onClick={onClick}><div className="task-card-top"><span className={`priority ${task.priority.toLowerCase()}`}><span />{task.priority}</span><button className="plain-button" onClick={(event) => event.stopPropagation()}><MoreHorizontal size={16} /></button></div><h3>{task.title}</h3><p>{task.description}</p><div className="tag-row">{task.tags.map((tag) => <span className="tag" key={tag}>{tag}</span>)}</div><div className="task-footer"><span className={task.blocked ? 'due overdue' : 'due'}><CalendarDays size={14} />{task.dueDate}</span><Avatar user={user} small /></div>{task.blocked && <div className="blocked-note">Blocked · {task.blocker}</div>}</article>
}

function TaskCard({ task, user, overlay = false }: { task: Task; user?: User; overlay?: boolean }) { return <div className={`task-card ${overlay ? 'overlay-card' : ''}`}><div className="task-card-top"><span className={`priority ${task.priority.toLowerCase()}`}><span />{task.priority}</span></div><h3>{task.title}</h3><div className="task-footer"><span className="due"><CalendarDays size={14} />{task.dueDate}</span><Avatar user={user} small /></div></div> }

function NotificationPanel({ items, onRead, onClose }: { items: { id: number; title: string; message: string; read: boolean; createdAt: string }[]; onRead: (id: number) => void; onClose: () => void }) { return <div className="notification-panel"><div className="panel-title"><strong>Notifications</strong><button className="plain-button" onClick={onClose}><X size={17} /></button></div>{items.map((item) => <button className={`notification ${item.read ? '' : 'unread'}`} key={item.id} onClick={() => onRead(item.id)}><span className="notification-icon"><Bell size={15} /></span><span><strong>{item.title}</strong><small>{item.message}</small><em>{item.createdAt}</em></span></button>)}</div> }

function TaskDrawer({ task, user, column, canEdit, onClose }: { task: Task; user?: User; column?: BoardColumn; canEdit: boolean; onClose: () => void }) {
  const ticketsQuery = useQuery({ queryKey: ['tickets', task.id], queryFn: () => ticketService.list(task.id) })
  const [showTicketForm, setShowTicketForm] = useState(false)
  const [editing, setEditing] = useState(false)
  const [title, setTitle] = useState(task.title)
  const [description, setDescription] = useState(task.description)
  const saveMutation = useMutation({ mutationFn: () => taskService.update(task.id, { title, description }), onSuccess: () => { setEditing(false); queryClient.invalidateQueries({ queryKey: ['board', 101] }) } })
  const ticketMutation = useMutation({ mutationFn: () => ticketService.create({ taskId: task.id, title: 'New ticket', description: 'Ticket details', statusColumnId: task.columnId, priority: 'MEDIUM', assigneeId: task.assigneeId, dueDate: task.dueDate }), onSuccess: () => { setShowTicketForm(false); queryClient.invalidateQueries({ queryKey: ['tickets', task.id] }) } })
  return <aside className="drawer"><div className="drawer-head"><span className="eyebrow">Task details · Project #{task.projectId}</span><button className="icon-button" onClick={onClose}><X size={18} /></button></div>{editing ? <><input className="drawer-edit-input" value={title} onChange={(event) => setTitle(event.target.value)} /><textarea className="drawer-edit-input" value={description} onChange={(event) => setDescription(event.target.value)} /></> : <><h2>{task.title}</h2><p className="drawer-description">{task.description}</p></>}<div className="drawer-actions">{canEdit && (editing ? <button className="button primary" onClick={() => saveMutation.mutate()} disabled={saveMutation.isPending}>Save</button> : <button className="button secondary" onClick={() => setEditing(true)}>Edit task</button>)}</div><div className="drawer-row"><span>Status</span><strong><span className={`column-dot ${column?.tone}`} />{column?.name}</strong></div><div className="drawer-row"><span>Assignee</span><strong><Avatar user={user} small /> {user?.name}</strong></div><div className="drawer-row"><span>Priority</span><strong className={`priority ${task.priority.toLowerCase()}`}><span />{task.priority}</strong></div><div className="drawer-row"><span>Due date</span><strong><CalendarDays size={15} /> {task.dueDate}</strong></div>{task.blocked && <div className="drawer-blocked"><strong>Blocked</strong><p>{task.blocker}</p></div>}<div className="ticket-section"><div className="section-inline"><h3>Tickets <span>{ticketsQuery.data?.length ?? 0}</span></h3>{canEdit && <button className="plain-button" onClick={() => setShowTicketForm(true)}><Plus size={16} /></button>}</div>{showTicketForm && <div className="ticket-form"><input defaultValue="New ticket" aria-label="Ticket title" /><button className="button primary" onClick={() => ticketMutation.mutate()}>Add</button></div>}{ticketsQuery.data?.map((ticket) => <TicketRow key={ticket.id} ticket={ticket} canEdit={canEdit} />)}</div><div className="activity"><h3>Activity</h3><div className="activity-item"><span className="activity-line" /><Avatar user={user} small /><p><strong>{user?.name}</strong> updated this task<small>{task.updatedAt}</small></p></div></div><button className="button primary full"><Check size={16} /> Mark as complete</button></aside>
}

function TicketRow({ ticket, canEdit }: { ticket: Ticket; canEdit: boolean }) {
  const [editing, setEditing] = useState(false)
  const [title, setTitle] = useState(ticket.title)
  const [description, setDescription] = useState(ticket.description)
  const mutation = useMutation({ mutationFn: () => ticketService.update(ticket.id, { title, description }), onSuccess: () => setEditing(false) })
  return <div className="ticket-row">{editing ? <div className="ticket-edit-fields"><input value={title} onChange={(event) => setTitle(event.target.value)} aria-label="Ticket title" /><textarea value={description} onChange={(event) => setDescription(event.target.value)} aria-label="Ticket description" /></div> : <div><strong>{ticket.title}</strong><small>{ticket.description}</small></div>}{canEdit && <button className="plain-button" onClick={() => editing ? mutation.mutate() : setEditing(true)} disabled={mutation.isPending}>{editing ? <Check size={14} /> : <Settings size={14} />}</button>}</div>
}

function CreateTask({ users, onClose, onCreated }: { users: User[]; onClose: () => void; onCreated: () => void }) { const [title, setTitle] = useState(''); const [assignee, setAssignee] = useState(users[1]?.id ?? 2); const [saving, setSaving] = useState(false); return <div className="modal-backdrop"><form className="modal" onSubmit={async (event) => { event.preventDefault(); if (!title.trim()) return; setSaving(true); await taskService.create({ title, description: 'New task created from the board.', assigneeId: assignee, priority: 'MEDIUM', dueDate: '2026-10-04', projectId: useAppStore.getState().selectedProjectId }); onCreated() }}><div className="modal-head"><div><span className="eyebrow">New task</span><h2>Create a task</h2></div><button type="button" className="icon-button" onClick={onClose}><X size={18} /></button></div><label>Task title<input autoFocus value={title} onChange={(event) => setTitle(event.target.value)} placeholder="What needs to be done?" required /></label><label>Assignee<select value={assignee} onChange={(event) => setAssignee(Number(event.target.value))}>{users.filter((user) => user.role !== 'ADMIN').map((user) => <option value={user.id} key={user.id}>{user.name}</option>)}</select></label><label>Due date<input type="date" defaultValue="2026-10-04" /></label><div className="modal-actions"><button type="button" className="button secondary" onClick={onClose}>Cancel</button><button className="button primary" disabled={saving}>{saving ? 'Creating...' : 'Create task'}</button></div></form></div> }

function Workload({ board, users }: { board: Board; users: User[] }) { return <><section className="page-heading"><div><div className="eyebrow">Team overview</div><h1>Workload</h1><p>See how work is distributed across your team.</p></div><button className="button secondary"><Download size={16} /> Export CSV</button></section><div className="summary-grid"><Metric label="Active tasks" value={String(board.tasks.filter((task) => task.columnId !== 4).length)} change="+12%" /><Metric label="Completed this week" value="18" change="+8%" /><Metric label="Overdue" value="2" change="-24%" danger /><Metric label="Blocked" value={String(board.tasks.filter((task) => task.blocked).length)} change="-10%" /></div><section className="surface workload-table"><div className="surface-head"><h2>Team capacity</h2><span>This week <ChevronDown size={15} /></span></div>{users.slice(1).map((user) => { const tasks = board.tasks.filter((task) => task.assigneeId === user.id && task.columnId !== 4); const pct = Math.min(tasks.length * 22, 100); return <div className="member-row" key={user.id}><Avatar user={user} /><div className="member-name"><strong>{user.name}</strong><small>{tasks.length} active tasks</small></div><div className="progress"><span style={{ width: `${pct}%`, background: user.color }} /></div><strong>{pct}%</strong><button className="plain-button"><MoreHorizontal size={17} /></button></div> })}</section></> }
function Metric({ label, value, change, danger = false }: { label: string; value: string; change: string; danger?: boolean }) { return <div className="metric"><span>{label}</span><strong>{value}</strong><small className={danger ? 'negative' : 'positive'}>{change} <span>vs last week</span></small></div> }
function Reports({ board }: { board: Board }) { const done = board.tasks.filter((task) => task.columnId === 4).length; return <><section className="page-heading"><div><div className="eyebrow">Insights</div><h1>Reports</h1><p>A snapshot of project progress and delivery health.</p></div><button className="button secondary"><Download size={16} /> Export report</button></section><div className="summary-grid"><Metric label="Progress" value={`${Math.round(done / board.tasks.length * 100)}%`} change="+6%" /><Metric label="Total tasks" value={String(board.tasks.length)} change="+3%" /><Metric label="Avg. cycle time" value="4.2d" change="-0.8d" /><Metric label="On-time rate" value="92%" change="+4%" /></div><section className="surface report-chart"><div className="surface-head"><h2>Tasks by status</h2><span>Last 30 days <ChevronDown size={15} /></span></div><div className="bars">{board.columns.map((column) => { const count = board.tasks.filter((task) => task.columnId === column.id).length; return <div className="bar-item" key={column.id}><div className={`bar ${column.tone}`} style={{ height: `${Math.max(count * 38, 28)}px` }}><span>{count}</span></div><small>{column.name}</small></div> })}</div></section></> }
function BoardSettings({ board }: { board: Board }) { const [columns, setColumns] = useState(board.columns); return <><section className="page-heading"><div><div className="eyebrow">Manage workflow</div><h1>Board settings</h1><p>Customize columns and workflow for this board.</p></div><button className="button primary" onClick={async () => { await boardService.updateColumns(columns) }}><Check size={16} /> Save changes</button></section><section className="surface settings-list"><div className="surface-head"><h2>Status columns</h2><button className="button secondary"><Plus size={15} /> Add column</button></div>{columns.map((column, index) => <div className="setting-row" key={column.id}><span className="drag-handle">⠿</span><span className={`column-dot ${column.tone}`} /><input value={column.name} onChange={(event) => setColumns(columns.map((item) => item.id === column.id ? { ...item, name: event.target.value } : item))} /><span className="setting-order">Position {index + 1}</span><button className="plain-button"><MoreHorizontal size={17} /></button></div>)}</section></> }

export default function App() { return <QueryClientProvider client={queryClient}><AppContent /></QueryClientProvider> }
