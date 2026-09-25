import { X } from 'lucide-react'
import type { User } from '../../types/domain'
import type { TaskFilters } from '../../lib/api/apiTransport'

interface FilterPanelProps {
  filters: TaskFilters
  onChange: (filters: TaskFilters) => void
  users: User[]
  onClose: () => void
}

export function FilterPanel({ filters, onChange, users, onClose }: FilterPanelProps) {
  const handleChange = (field: keyof TaskFilters, value: any) => {
    onChange({ ...filters, [field]: value })
  }

  const handleClear = () => {
    onChange({})
  }

  return (
    <div className="filter-panel">
      <div className="filter-panel-head">
        <h3>Filter Tasks</h3>
        <button onClick={onClose}>
          <X size={16} />
        </button>
      </div>

      <div className="filter-group">
        <label>Assignee</label>
        <select 
          value={filters.assigneeId || ''} 
          onChange={(e) => handleChange('assigneeId', e.target.value)}
        >
          <option value="">All Assignees</option>
          {users.map(u => (
            <option key={u.id} value={u.id}>{u.name}</option>
          ))}
        </select>
      </div>

      <div className="filter-group">
        <label>Assigned Date</label>
        <div className="filter-input-row">
          <input 
            type="date" 
            value={filters.assignedDateFrom ? new Date(filters.assignedDateFrom).toISOString().split('T')[0] : ''}
            onChange={(e) => handleChange('assignedDateFrom', e.target.value ? new Date(e.target.value).toISOString() : undefined)}
            title="From"
          />
          <input 
            type="date" 
            value={filters.assignedDateTo ? new Date(filters.assignedDateTo).toISOString().split('T')[0] : ''}
            onChange={(e) => handleChange('assignedDateTo', e.target.value ? new Date(e.target.value + 'T23:59:59Z').toISOString() : undefined)}
            title="To"
          />
        </div>
      </div>

      <div className="filter-group">
        <label>Start Date</label>
        <div className="filter-input-row">
          <input 
            type="date" 
            value={filters.startDateFrom ? new Date(filters.startDateFrom).toISOString().split('T')[0] : ''}
            onChange={(e) => handleChange('startDateFrom', e.target.value ? new Date(e.target.value).toISOString() : undefined)}
            title="From"
          />
          <input 
            type="date" 
            value={filters.startDateTo ? new Date(filters.startDateTo).toISOString().split('T')[0] : ''}
            onChange={(e) => handleChange('startDateTo', e.target.value ? new Date(e.target.value + 'T23:59:59Z').toISOString() : undefined)}
            title="To"
          />
        </div>
      </div>

      <div className="filter-group">
        <label>End Date (Due Date)</label>
        <div className="filter-input-row">
          <input 
            type="date" 
            value={filters.endDateFrom ? new Date(filters.endDateFrom).toISOString().split('T')[0] : ''}
            onChange={(e) => handleChange('endDateFrom', e.target.value ? new Date(e.target.value).toISOString() : undefined)}
            title="From"
          />
          <input 
            type="date" 
            value={filters.endDateTo ? new Date(filters.endDateTo).toISOString().split('T')[0] : ''}
            onChange={(e) => handleChange('endDateTo', e.target.value ? new Date(e.target.value + 'T23:59:59Z').toISOString() : undefined)}
            title="To"
          />
        </div>
      </div>

      <div className="filter-actions">
        <button onClick={handleClear} className="filter-clear">
          Clear All
        </button>
      </div>
    </div>
  )
}
