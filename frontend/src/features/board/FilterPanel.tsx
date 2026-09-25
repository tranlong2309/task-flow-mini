import { X, Calendar, User as UserIcon, Flag, AlertTriangle } from 'lucide-react'
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
    const cleaned = value === '' || value === undefined ? undefined : value
    onChange({ ...filters, [field]: cleaned })
  }

  const handleClear = () => onChange({})

  const activeCount = Object.values(filters).filter(v => v !== undefined && v !== '').length

  return (
    <div className="filter-panel">
      <div className="filter-panel-head">
        <h3>
          Filter Tasks
          {activeCount > 0 && (
            <span style={{
              marginLeft: '8px',
              background: '#5954c8',
              color: '#fff',
              fontSize: '10px',
              fontWeight: 700,
              borderRadius: '20px',
              padding: '2px 7px'
            }}>{activeCount}</span>
          )}
        </h3>
        <button onClick={onClose}>
          <X size={16} />
        </button>
      </div>

      {/* Assignee */}
      <div className="filter-group">
        <label><UserIcon size={12} /> Assignee</label>
        <select
          value={filters.assigneeId ?? ''}
          onChange={(e) => handleChange('assigneeId', e.target.value ? Number(e.target.value) : undefined)}
        >
          <option value="">All Assignees</option>
          {users.map(u => (
            <option key={u.id} value={u.id}>{u.name}</option>
          ))}
        </select>
      </div>

      {/* Priority */}
      <div className="filter-group">
        <label><Flag size={12} /> Priority</label>
        <select
          value={filters.priority ?? ''}
          onChange={(e) => handleChange('priority', e.target.value || undefined)}
        >
          <option value="">All Priorities</option>
          <option value="HIGH">🔴 High</option>
          <option value="MEDIUM">🟡 Medium</option>
          <option value="LOW">🟢 Low</option>
        </select>
      </div>

      {/* Overdue */}
      <div className="filter-group">
        <label style={{ display: 'flex', alignItems: 'center', gap: '4px', justifyContent: 'space-between', cursor: 'pointer' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <AlertTriangle size={12} /> Overdue Only
          </span>
          <input
            type="checkbox"
            checked={filters.overdueOnly === true}
            onChange={(e) => handleChange('overdueOnly', e.target.checked ? true : undefined)}
            style={{ width: '14px', height: '14px', accentColor: '#5954c8', cursor: 'pointer' }}
          />
        </label>
      </div>

      {/* Due Date */}
      <div className="filter-group">
        <label><Calendar size={12} /> Due Date</label>
        <div className="filter-input-row">
          <input
            type="date"
            value={filters.endDateFrom ? new Date(filters.endDateFrom).toISOString().split('T')[0] : ''}
            onChange={(e) => handleChange('endDateFrom', e.target.value ? new Date(e.target.value).toISOString() : undefined)}
          />
          <span style={{ color: '#a09eae', fontSize: '10px', alignSelf: 'center' }}>to</span>
          <input
            type="date"
            value={filters.endDateTo ? new Date(filters.endDateTo).toISOString().split('T')[0] : ''}
            onChange={(e) => handleChange('endDateTo', e.target.value ? new Date(e.target.value + 'T23:59:59Z').toISOString() : undefined)}
          />
        </div>
      </div>

      {/* Assigned Date */}
      <div className="filter-group">
        <label><Calendar size={12} /> Assigned Date</label>
        <div className="filter-input-row">
          <input
            type="date"
            value={filters.assignedDateFrom ? new Date(filters.assignedDateFrom).toISOString().split('T')[0] : ''}
            onChange={(e) => handleChange('assignedDateFrom', e.target.value ? new Date(e.target.value).toISOString() : undefined)}
          />
          <span style={{ color: '#a09eae', fontSize: '10px', alignSelf: 'center' }}>to</span>
          <input
            type="date"
            value={filters.assignedDateTo ? new Date(filters.assignedDateTo).toISOString().split('T')[0] : ''}
            onChange={(e) => handleChange('assignedDateTo', e.target.value ? new Date(e.target.value + 'T23:59:59Z').toISOString() : undefined)}
          />
        </div>
      </div>

      <div className="filter-actions">
        <button
          className="filter-clear"
          onClick={handleClear}
          disabled={activeCount === 0}
          style={{ opacity: activeCount === 0 ? 0.5 : 1 }}
        >
          Clear All Filters
        </button>
      </div>
    </div>
  )
}
