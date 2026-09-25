import { create } from 'zustand'

type View = 'board' | 'workload' | 'reports' | 'settings'
interface AppState {
  view: View
  search: string
  priority: string
  selectedTaskId: number | null
  selectedProjectId: number
  setView: (view: View) => void
  setSearch: (search: string) => void
  setPriority: (priority: string) => void
  setSelectedTaskId: (id: number | null) => void
  setSelectedProjectId: (id: number) => void
}

export const useAppStore = create<AppState>((set) => ({
  view: 'board',
  search: '',
  priority: 'all',
  selectedTaskId: null,
  selectedProjectId: 11,
  setView: (view) => set({ view }),
  setSearch: (search) => set({ search }),
  setPriority: (priority) => set({ priority }),
  setSelectedTaskId: (selectedTaskId) => set({ selectedTaskId }),
  setSelectedProjectId: (selectedProjectId) => set({ selectedProjectId, selectedTaskId: null }),
}))
