import { useEffect, useMemo, useState } from 'react'
import { QueryClient, QueryClientProvider, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { DndContext, DragOverlay, PointerSensor, useSensor, useSensors, type DragEndEvent } from '@dnd-kit/core'
import { Bell, ChevronDown, CircleHelp, Filter, FolderKanban, LayoutDashboard, MoreHorizontal, Plus, Search, Settings, Sparkles, Target, Users } from 'lucide-react'
import { authService, boardService, notificationService, projectService, taskService } from './lib/api/services'
import { useAppStore } from './stores/useAppStore'
import { can, canEditTask, useAuthStore } from './stores/authStore'
import type { Board, User } from './types/domain'

import { Avatar } from './components/ui/Avatar'
import { LoginScreen } from './features/auth/LoginScreen'
import { NotificationPanel } from './components/notifications/NotificationPanel'
import { KanbanColumn } from './features/board/KanbanColumn'
import { TaskCard } from './features/tasks/TaskCard'
import { TaskDrawer } from './features/tasks/TaskDrawer'
import { CreateTask } from './features/tasks/CreateTask'
import { Workload } from './features/workload/Workload'
import { Reports } from './features/reports/Reports'
import { BoardSettings } from './features/settings/BoardSettings'
import { ProjectManagement } from './features/settings/ProjectManagement'
import { EmployeeManagement } from './features/settings/EmployeeManagement'
import { FilterPanel } from './features/board/FilterPanel'
import type { TaskFilters } from './lib/api/apiTransport'

import './App.css'

const queryClient = new QueryClient({ defaultOptions: { queries: { staleTime: 30_000 } } })

function AppContent() {
  const { user, token, hydrate, setSession, logout } = useAuthStore()
  const [loginEmail, setLoginEmail] = useState('linh@acme.test')
  const [loginPassword, setLoginPassword] = useState('password')
  const [loginError, setLoginError] = useState('')
  const [loggingIn, setLoggingIn] = useState(false)
  
  useEffect(() => { hydrate() }, [hydrate])
  
  if (!token || !user) {
    return (
      <LoginScreen 
        email={loginEmail} 
        password={loginPassword} 
        error={loginError} 
        loading={loggingIn} 
        onEmail={setLoginEmail} 
        onPassword={setLoginPassword} 
        onSubmit={async (event) => { 
          event.preventDefault(); 
          setLoggingIn(true); 
          setLoginError(''); 
          try { 
            setSession(await authService.login(loginEmail, loginPassword)) 
          } catch { 
            setLoginError('Invalid email or password. Use password for the demo.') 
          } finally { 
            setLoggingIn(false) 
          } 
        }} 
      />
    )
  }
  
  return <AuthenticatedApp user={user} logout={logout} />
}

function AuthenticatedApp({ user, logout }: { user: User; logout: () => void }) {
  const qc = useQueryClient()
  const { view, setView, search, setSearch, priority, setPriority, selectedTaskId, setSelectedTaskId, selectedProjectId, setSelectedProjectId } = useAppStore()
  
  const [taskFilters, setTaskFilters] = useState<TaskFilters>({})
  const [showFilters, setShowFilters] = useState(false)
  const [debouncedSearch, setDebouncedSearch] = useState(search)

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(search), 300)
    return () => clearTimeout(timer)
  }, [search])

  const boardQuery = useQuery({
    queryKey: ['board', selectedProjectId, taskFilters, debouncedSearch],
    queryFn: () => boardService.get(selectedProjectId, {
      ...taskFilters,
      search: debouncedSearch || undefined,
    })
  })
  const usersQuery = useQuery({ queryKey: ['users'], queryFn: taskService.listUsers })
  const projectQuery = useQuery({ queryKey: ['projects'], queryFn: projectService.list })
  const notificationQuery = useQuery({ queryKey: ['notifications'], queryFn: notificationService.list })
  
  const [activeId, setActiveId] = useState<string | number | null>(null)
  const [showNotifications, setShowNotifications] = useState(false)
  const [showCreate, setShowCreate] = useState(false)
  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 5 } }))
  
  const board = boardQuery.data
  const projects = projectQuery.data ?? []
  const users = usersQuery.data ?? []
  const unread = notificationQuery.data?.filter((item) => !item.read).length ?? 0
  
  const moveMutation = useMutation({
    mutationFn: ({ taskId, columnId }: { taskId: string | number; columnId: string | number }) => boardService.moveTask(taskId, columnId),
    onMutate: async ({ taskId, columnId }) => {
      await qc.cancelQueries({ queryKey: ['board', selectedProjectId] })
      const previous = qc.getQueryData<Board>(['board', selectedProjectId])
      qc.setQueryData<Board>(['board', selectedProjectId], (current) => 
        current && ({ ...current, tasks: current.tasks.map((task) => task.id === taskId ? { ...task, columnId, updatedAt: 'Just now' } : task) })
      )
      return { previous }
    },
    onError: (_error, _variables, context) => qc.setQueryData(['board', selectedProjectId], context?.previous),
    onSettled: () => qc.invalidateQueries({ queryKey: ['board', selectedProjectId] }),
  })

  const filteredTasks = board?.tasks ?? []

  if (boardQuery.isLoading) return <div className="loading-screen"><Sparkles size={20} /> Loading your workspace...</div>
  if (boardQuery.isError || !board) return <div className="loading-screen error">Could not load the board. Please try again.</div>

  const selectedTask = board.tasks.find((task) => task.id === selectedTaskId)
  const activeTask = board.tasks.find((task) => task.id === activeId)

  function handleDragEnd(event: DragEndEvent) {
    setActiveId(null)
    const { active, over } = event
    if (!over || active.id === over.id) return
    const column = board?.columns.find((item) => String(item.id) === String(over.id))
    if (column) {
      moveMutation.mutate({ taskId: active.id, columnId: column.id })
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark"><Sparkles size={16} /></div>
          <span>taskflow</span>
        </div>
        <div className="workspace-switcher" style={{ position: 'relative' }}>
          <div className="workspace-icon" style={{ background: '#7e64f2' }}>P</div>
          <div style={{ flex: 1, overflow: 'hidden' }}>
            <strong>Project</strong>
            <small style={{ display: 'block', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden' }}>
              {projects.find(p => p.id === selectedProjectId)?.name || 'Select a project'}
            </small>
          </div>
          <ChevronDown size={15} />
          <select 
            value={selectedProjectId} 
            onChange={(event) => setSelectedProjectId(event.target.value)}
            style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', opacity: 0, cursor: 'pointer' }}
          >
            {projects.map((project) => <option value={project.id} key={project.id}>{project.name}</option>)}
          </select>
        </div>
        <nav className="nav">
          <p className="nav-label">Project</p>
          <button className={view === 'board' ? 'nav-item active' : 'nav-item'} onClick={() => setView('board')}>
            <LayoutDashboard size={18} /> My board <span className="nav-count">{board.tasks.length}</span>
          </button>
          <button className={view === 'workload' ? 'nav-item active' : 'nav-item'} onClick={() => setView('workload')}>
            <Users size={18} /> Workload
          </button>
          <button className={view === 'reports' ? 'nav-item active' : 'nav-item'} onClick={() => setView('reports')}>
            <Target size={18} /> Reports
          </button>
          <p className="nav-label">Manage</p>
          {can(user.role, 'manageBoard') && (
            <>
              <button className={view === 'projects' ? 'nav-item active' : 'nav-item'} onClick={() => setView('projects')}>
                <FolderKanban size={18} /> Projects
              </button>
              <button className={view === 'employees' ? 'nav-item active' : 'nav-item'} onClick={() => setView('employees')}>
                <Users size={18} /> Employees
              </button>
              <button className={view === 'settings' ? 'nav-item active' : 'nav-item'} onClick={() => setView('settings')}>
                <Settings size={18} /> Board settings
              </button>
            </>
          )}
        </nav>
        <div className="sidebar-bottom">
          <div className="help-card">
            <CircleHelp size={17} />
            <div><strong>Need a hand?</strong><small>Visit help center</small></div>
          </div>
          <button className="profile" onClick={logout}>
            <Avatar user={user} />
            <div><strong>{user.name}</strong><small>{user.role}</small></div>
            <span className="logout-label">Log out</span>
          </button>
        </div>
      </aside>
      
      <main className="main">
        <header className="topbar">
          <div className="breadcrumbs">
            <span>Projects</span><span>/</span><strong>{projects.find(p => p.id === selectedProjectId)?.name || 'Project'}</strong>
          </div>
          <div className="top-actions">
            <label className="search">
              <Search size={17} />
              <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search tasks..." />
              <kbd>⌘ K</kbd>
            </label>
            <button className="icon-button" onClick={() => setShowNotifications(!showNotifications)} aria-label="Notifications">
              <Bell size={19} />
              {unread > 0 && <i>{unread}</i>}
            </button>
            <Avatar user={users[0]} />
          </div>
        </header>
        
        {showNotifications && (
          <NotificationPanel 
            items={notificationQuery.data ?? []} 
            onRead={async (id) => { 
              await notificationService.markRead(id); 
              qc.invalidateQueries({ queryKey: ['notifications'] }) 
            }} 
            onClose={() => setShowNotifications(false)} 
          />
        )}
        
        {view === 'board' && (
          <>
            <section className="page-heading">
              <div>
                <div className="eyebrow">Project board <span className="live-dot" /> Live</div>
                <h1>{board.name}</h1>
                <p>{board.description}</p>
              </div>
              <div className="heading-actions" style={{ position: 'relative' }}>
                <button className="button secondary" onClick={() => setShowFilters(!showFilters)}>
                  <Filter size={16} /> Filter
                </button>
                {showFilters && (
                  <FilterPanel 
                    filters={taskFilters} 
                    onChange={setTaskFilters} 
                    users={users} 
                    onClose={() => setShowFilters(false)} 
                  />
                )}
                <button className="button primary" onClick={() => setShowCreate(true)}>
                  <Plus size={17} /> New task
                </button>
              </div>
            </section>
            
            <div className="board-toolbar">
              <div className="filter-chips">
                <span>Showing <strong>{filteredTasks.length} tasks</strong></span>
                <button
                  className={!taskFilters.priority ? 'chip selected' : 'chip'}
                  onClick={() => setTaskFilters(f => ({ ...f, priority: undefined }))}
                >All</button>
                {(['HIGH', 'MEDIUM', 'LOW'] as const).map((item) => (
                  <button
                    key={item}
                    className={taskFilters.priority === item ? 'chip selected' : 'chip'}
                    onClick={() => setTaskFilters(f => ({ ...f, priority: taskFilters.priority === item ? undefined : item }))}
                  >
                    {item[0] + item.slice(1).toLowerCase()}
                  </button>
                ))}
              </div>
              <div className="board-meta">
                <span><span className="green-dot" /> Synced just now</span>
                <button className="icon-button"><MoreHorizontal size={18} /></button>
              </div>
            </div>
            
            <DndContext sensors={sensors} onDragStart={(event) => setActiveId(event.active.id)} onDragEnd={handleDragEnd} onDragCancel={() => setActiveId(null)}>
              <div className="kanban">
                {[...board.columns].sort((a, b) => a.order - b.order).map((column) => (
                  <KanbanColumn 
                    key={column.id} 
                    column={column} 
                    tasks={filteredTasks.filter((task) => String(task.statusColumnId) === String(column.id))} 
                    users={users} 
                    onTaskClick={setSelectedTaskId} 
                  />
                ))}
              </div>
              <DragOverlay>
                {activeTask ? (
                  <TaskCard task={activeTask} users={users.filter(u => activeTask.assigneeIds?.includes(u.id) || String(u.id) === String(activeTask.assigneeId))} overlay />
                ) : null}
              </DragOverlay>
            </DndContext>
          </>
        )}
        
        {view === 'workload' && <Workload board={board} users={users} />}
        {view === 'reports' && <Reports board={board} />}
        {view === 'settings' && <BoardSettings board={board} />}
        {view === 'projects' && <ProjectManagement projects={projects} />}
        {view === 'employees' && <EmployeeManagement users={users} />}
      </main>
      
      {selectedTask && (
        <TaskDrawer 
          task={selectedTask} 
          user={users.find((item) => String(item.id) === String(selectedTask.assigneeId))} 
          column={board.columns.find((item) => String(item.id) === String(selectedTask.columnId))} 
          canEdit={canEditTask(user || null, selectedTask)} 
          onClose={() => setSelectedTaskId(null)} 
        />
      )}
      
      {showCreate && (
        <CreateTask 
          users={users} 
          onClose={() => setShowCreate(false)} 
          onCreated={() => { 
            setShowCreate(false); 
            qc.invalidateQueries({ queryKey: ['board', selectedProjectId] }) 
          }} 
        />
      )}
    </div>
  )
}

export default function App() { 
  return (
    <QueryClientProvider client={queryClient}>
      <AppContent />
    </QueryClientProvider>
  )
}
