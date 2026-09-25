import { Download, ChevronDown } from 'lucide-react'
import type { Board } from '../../types/domain'
import { Metric } from '../../components/ui/Metric'

export function Reports({ board }: { board: Board }) { 
  const totalTasks = board.tasks.length;
  const completedTasks = board.tasks.filter(t => t.completedAt);
  const doneCount = completedTasks.length; 
  const completionRate = totalTasks ? Math.round(doneCount / totalTasks * 100) : 0;
  
  // Calculate Avg. cycle time
  let totalCycleTimeMs = 0;
  completedTasks.forEach(t => {
    const start = new Date(t.createdAt || t.updatedAt).getTime();
    const end = new Date(t.completedAt!).getTime();
    if (end > start) {
      totalCycleTimeMs += (end - start);
    }
  });
  const avgCycleTimeDays = doneCount > 0 
    ? (totalCycleTimeMs / doneCount / (1000 * 60 * 60 * 24)).toFixed(1) 
    : '0.0';

  // Calculate On-time rate
  const completedWithDueDate = completedTasks.filter(t => t.dueDate);
  const onTimeCount = completedWithDueDate.filter(t => new Date(t.completedAt!) <= new Date(t.dueDate)).length;
  const onTimeRate = completedWithDueDate.length > 0 
    ? Math.round(onTimeCount / completedWithDueDate.length * 100) 
    : 100; // If no due dates, technically 100% on time

  const handleExportCSV = () => {
    const headers = ['Status Column', 'Task Count'];
    const rows = board.columns.map(column => {
      const count = board.tasks.filter((task) => String(task.statusColumnId) === String(column.id)).length;
      return [`"${column.name}"`, count].join(',');
    });
    
    // Add summary to CSV
    const summaryHeaders = ['Metric', 'Value'];
    const summaryRows = [
      ['Progress', `${completionRate}%`],
      ['Total tasks', totalTasks],
      ['Avg. cycle time', `${avgCycleTimeDays}d`],
      ['On-time rate', `${onTimeRate}%`]
    ].map(r => r.join(','));

    const csvContent = [
      summaryHeaders.join(','), 
      ...summaryRows, 
      '', // empty line
      headers.join(','), 
      ...rows
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `board_report_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <>
      <section className="page-heading">
        <div>
          <div className="eyebrow">Insights</div>
          <h1>Reports</h1>
          <p>A snapshot of project progress and delivery health.</p>
        </div>
        <button className="button secondary" onClick={handleExportCSV}>
          <Download size={16} /> Export report
        </button>
      </section>
      <div className="summary-grid">
        <Metric label="Progress" value={`${completionRate}%`} />
        <Metric label="Total tasks" value={String(totalTasks)} />
        <Metric label="Avg. cycle time" value={`${avgCycleTimeDays}d`} />
        <Metric label="On-time rate" value={`${onTimeRate}%`} />
      </div>
      <section className="surface report-chart">
        <div className="surface-head">
          <h2>Tasks by status</h2>
          <span>All time</span>
        </div>
        <div className="bars">
          {board.columns.map((column) => { 
            const count = board.tasks.filter((task) => String(task.statusColumnId) === String(column.id)).length; 
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
