import { Download, ChevronDown, MoreHorizontal } from 'lucide-react'
import type { Board, User } from '../../types/domain'
import { Metric } from '../../components/ui/Metric'
import { Avatar } from '../../components/ui/Avatar'

export function Workload({ board, users }: { board: Board; users: User[] }) { 
  const now = new Date();
  const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);

  // Global board metrics
  const activeTasksCount = board.tasks.filter((t) => !t.completedAt).length;
  const completedThisWeekCount = board.tasks.filter((t) => t.completedAt && new Date(t.completedAt) > oneWeekAgo).length;
  const overdueCount = board.tasks.filter((t) => !t.completedAt && t.dueDate && new Date(t.dueDate) < now).length;
  const blockedCount = board.tasks.filter((t) => t.blocked || (t as any).isBlocked).length;

  const handleExportCSV = () => {
    const headers = ['Name', 'Email', 'Role', 'Active Tasks', 'Completed (All Time)', 'Overdue', 'Blocked', 'Capacity (%)'];
    const rows = users.map(user => {
      const userTasks = board.tasks.filter((task) => 
        task.assigneeIds?.some(id => String(id) === String(user.id)) || String(task.assigneeId) === String(user.id)
      );

      const active = userTasks.filter(t => !t.completedAt).length;
      const completed = userTasks.filter(t => t.completedAt).length;
      const overdue = userTasks.filter(t => !t.completedAt && t.dueDate && new Date(t.dueDate) < now).length;
      const blocked = userTasks.filter(t => t.blocked || (t as any).isBlocked).length;
      const pct = Math.min(active * 20, 100); // 5 active tasks = 100% capacity

      return [
        `"${user.name}"`,
        `"${user.email}"`,
        `"${user.role}"`,
        active,
        completed,
        overdue,
        blocked,
        pct
      ].join(',');
    });
    
    const csvContent = [headers.join(','), ...rows].join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `workload_export_${now.toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <>
      <section className="page-heading">
        <div>
          <div className="eyebrow">Team overview</div>
          <h1>Workload</h1>
          <p>See how work is distributed across your team.</p>
        </div>
        <button className="button secondary" onClick={handleExportCSV}>
          <Download size={16} /> Export CSV
        </button>
      </section>
      
      <div className="summary-grid">
        <Metric 
          label="Active tasks" 
          value={String(activeTasksCount)} 
        />
        <Metric 
          label="Completed this week" 
          value={String(completedThisWeekCount)} 
        />
        <Metric 
          label="Overdue" 
          value={String(overdueCount)} 
          danger={overdueCount > 0} 
        />
        <Metric 
          label="Blocked" 
          value={String(blockedCount)} 
          danger={blockedCount > 0}
        />
      </div>
      
      <section className="surface workload-table">
        <div className="surface-head">
          <h2>Team capacity</h2>
          <span>This week <ChevronDown size={15} /></span>
        </div>
        {users.map((user) => { 
          const userTasks = board.tasks.filter((task) => 
            task.assigneeIds?.some(id => String(id) === String(user.id)) || String(task.assigneeId) === String(user.id)
          );
          
          const activeTasks = userTasks.filter(t => !t.completedAt);
          const overdueTasks = activeTasks.filter(t => t.dueDate && new Date(t.dueDate) < now);
          const blockedTasks = userTasks.filter(t => t.blocked || (t as any).isBlocked);

          const pct = Math.min(activeTasks.length * 20, 100); 
          let statusText = `${activeTasks.length} active`;
          if (overdueTasks.length > 0) statusText += `, ${overdueTasks.length} overdue`;
          if (blockedTasks.length > 0) statusText += `, ${blockedTasks.length} blocked`;

          return (
            <div className="member-row" key={user.id}>
              <Avatar user={user} />
              <div className="member-name">
                <strong>{user.name}</strong>
                <small style={{ color: overdueTasks.length > 0 || blockedTasks.length > 0 ? 'var(--danger-color, #e02424)' : 'inherit' }}>
                  {statusText}
                </small>
              </div>
              <div className="progress">
                <span style={{ 
                  width: `${pct}%`, 
                  background: pct >= 100 ? 'var(--danger-color, #e02424)' : user.color 
                }} />
              </div>
              <strong style={{ color: pct >= 100 ? 'var(--danger-color, #e02424)' : 'inherit' }}>{pct}%</strong>
              <button className="plain-button"><MoreHorizontal size={17} /></button>
            </div>
          ) 
        })}
      </section>
    </>
  ) 
}
