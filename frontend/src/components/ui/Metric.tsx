
export function Metric({ 
  label, 
  value, 
  change, 
  danger = false 
}: { 
  label: string; 
  value: string; 
  change: string; 
  danger?: boolean 
}) { 
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
      <small className={danger ? 'negative' : 'positive'}>
        {change} <span>vs last week</span>
      </small>
    </div>
  ) 
}
