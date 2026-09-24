import { Bell, X } from 'lucide-react'

interface NotificationItem {
  id: string | number
  title: string
  message: string
  read: boolean
  createdAt: string
}

interface NotificationPanelProps {
  items: NotificationItem[]
  onRead: (id: string | number) => void
  onClose: () => void
}

export function NotificationPanel({ items, onRead, onClose }: NotificationPanelProps) { 
  return (
    <div className="notification-panel">
      <div className="panel-title">
        <strong>Notifications</strong>
        <button className="plain-button" onClick={onClose}>
          <X size={17} />
        </button>
      </div>
      {items.map((item) => (
        <button 
          className={`notification ${item.read ? '' : 'unread'}`} 
          key={item.id} 
          onClick={() => onRead(item.id)}
        >
          <span className="notification-icon">
            <Bell size={15} />
          </span>
          <span>
            <strong>{item.title}</strong>
            <small>{item.message}</small>
            <em>{item.createdAt}</em>
          </span>
        </button>
      ))}
    </div>
  )
}
