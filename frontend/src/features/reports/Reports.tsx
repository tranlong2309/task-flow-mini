import { Download, ChevronDown } from 'lucide-react'
import type { Board } from '../../types/domain'
import { Metric } from '../../components/ui/Metric'

export function Reports({ board }: { board: Board }) { 
  const done = board.tasks.filter((task) => String(task.columnId) === '4').length; 
  const completionRate = board.tasks.length ? Math.round(done / board.tasks.length * 100) : 0;
  
  return (
    <>
      <section className="page-heading">
        <div>
          <div className="eyebrow">Insights</div>
          <h1>Reports</h1>
          <p>A snapshot of project progress and delivery health.</p>
        </div>
        <button className="button secondary"><Download size={16} /> Export report</button>
      </section>
      <div className="summary-grid">
        <Metric label="Progress" value={`${completionRate}%`} change="+6%" />
        <Metric label="Total tasks" value={String(board.tasks.length)} change="+3%" />
        <Metric label="Avg. cycle time" value="4.2d" change="-0.8d" />
        <Metric label="On-time rate" value="92%" change="+4%" />
      </div>
      <section className="surface report-chart">
        <div className="surface-head">
          <h2>Tasks by status</h2>
          <span>Last 30 days <ChevronDown size={15} /></span>
        </div>
        <div className="bars">
          {board.columns.map((column) => { 
            const count = board.tasks.filter((task) => task.columnId === column.id).length; 
            return (
              <div className="bar-item" key={column.id}>
                <div className={`bar ${column.tone}`} style={{ height: `${Math.max(count * 38, 28)}px` }}>
                  <span>{count}</span>
                </div>
                <small>{column.name}</small>
              </div>
            ) 
          })}
        </div>
      </section>
    </>
  ) 
}
