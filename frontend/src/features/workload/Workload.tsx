import { Download, ChevronDown, MoreHorizontal } from 'lucide-react'
import type { Board, User } from '../../types/domain'
import { Metric } from '../../components/ui/Metric'
import { Avatar } from '../../components/ui/Avatar'

export function Workload({ board, users }: { board: Board; users: User[] }) { 
  return (
    <>
      <section className="page-heading">
        <div>
          <div className="eyebrow">Team overview</div>
          <h1>Workload</h1>
          <p>See how work is distributed across your team.</p>
        </div>
        <button className="button secondary">
          <Download size={16} /> Export CSV
        </button>
      </section>
      
      <div className="summary-grid">
        <Metric 
          label="Active tasks" 
          value={String(board.tasks.filter((task) => task.statusColumnId !== 4).length)} 
          change="+12%" 
        />
        <Metric label="Completed this week" value="18" change="+8%" />
        <Metric label="Overdue" value="2" change="-24%" danger />
        <Metric 
          label="Blocked" 
          value={String(board.tasks.filter((task) => task.blocked).length)} 
          change="-10%" 
        />
      </div>
      
      <section className="surface workload-table">
        <div className="surface-head">
          <h2>Team capacity</h2>
          <span>This week <ChevronDown size={15} /></span>
        </div>
        {users.slice(1).map((user) => { 
          const tasks = board.tasks.filter((task) => task.assigneeId === user.id && task.statusColumnId !== 4); 
          const pct = Math.min(tasks.length * 22, 100); 
          return (
            <div className="member-row" key={user.id}>
              <Avatar user={user} />
              <div className="member-name">
                <strong>{user.name}</strong>
                <small>{tasks.length} active tasks</small>
              </div>
              <div className="progress">
                <span style={{ width: `${pct}%`, background: user.color }} />
              </div>
              <strong>{pct}%</strong>
              <button className="plain-button"><MoreHorizontal size={17} /></button>
            </div>
          ) 
        })}
      </section>
    </>
  ) 
}
