import { useState } from 'react'
import { Check, Settings } from 'lucide-react'
import { useMutation } from '@tanstack/react-query'
import type { Ticket } from '../../types/domain'
import { ticketService } from '../../lib/api/services'

export function TicketRow({ ticket, canEdit }: { ticket: Ticket; canEdit: boolean }) {
  const [editing, setEditing] = useState(false)
  const [title, setTitle] = useState(ticket.title)
  const [description, setDescription] = useState(ticket.description)
  
  const mutation = useMutation({ 
    mutationFn: () => ticketService.update(ticket.id, { title, description }), 
    onSuccess: () => setEditing(false) 
  })
  
  return (
    <div className="ticket-row">
      {editing ? (
        <div className="ticket-edit-fields">
          <input 
            value={title} 
            onChange={(event) => setTitle(event.target.value)} 
            aria-label="Ticket title" 
          />
          <textarea 
            value={description} 
            onChange={(event) => setDescription(event.target.value)} 
            aria-label="Ticket description" 
          />
        </div>
      ) : (
        <div>
          <strong>{ticket.title}</strong>
          <small>{ticket.description}</small>
        </div>
      )}
      
      {canEdit && (
        <button 
          className="plain-button" 
          onClick={() => editing ? mutation.mutate() : setEditing(true)} 
          disabled={mutation.isPending}
        >
          {editing ? <Check size={14} /> : <Settings size={14} />}
        </button>
      )}
    </div>
  )
}
