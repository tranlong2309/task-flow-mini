import { create } from 'zustand'

type View = 'board' | 'workload' | 'reports' | 'settings' | 'projects' | 'employees'
interface AppState {
  view: View
  search: string
  priority: string
  selectedTaskId: string | number | null
  selectedProjectId: string | number
  setView: (view: View) => void
  setSearch: (search: string) => void
  setPriority: (priority: string) => void
  setSelectedTaskId: (id: string | number | null) => void
  setSelectedProjectId: (id: string | number) => void
}

export const useAppStore = create<AppState>((set) => ({
  view: 'board',
  search: '',
  priority: 'all',
  selectedTaskId: null,
  selectedProjectId: '123e4567-e89b-12d3-a456-426614174000',
  setView: (view) => set({ view }),
  setSearch: (search) => set({ search }),
  setPriority: (priority) => set({ priority }),
  setSelectedTaskId: (selectedTaskId) => set({ selectedTaskId }),
  setSelectedProjectId: (selectedProjectId) => set({ selectedProjectId, selectedTaskId: null }),
}))
