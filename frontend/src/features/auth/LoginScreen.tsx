import { type FormEvent } from 'react'
import { Sparkles } from 'lucide-react'

interface LoginScreenProps {
  email: string
  password: string
  error: string
  loading: boolean
  onEmail: (value: string) => void
  onPassword: (value: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

export function LoginScreen({ email, password, error, loading, onEmail, onPassword, onSubmit }: LoginScreenProps) {
  return (
    <div className="login-screen">
      <div className="login-card">
        <div className="brand login-brand">
          <div className="brand-mark">
            <Sparkles size={16} />
          </div>
          <span>taskflow</span>
        </div>
        <span className="eyebrow">Welcome back</span>
        <h1>Sign in to your workspace</h1>
        <p>Use one of the demo accounts to explore role-based access.</p>
        <form onSubmit={onSubmit}>
          <label>
            Email
            <input type="email" value={email} onChange={(event) => onEmail(event.target.value)} />
          </label>
          <label>
            Password
            <input type="password" value={password} onChange={(event) => onPassword(event.target.value)} />
          </label>
          {error && <div className="form-error">{error}</div>}
          <button className="button primary full" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
        <div className="demo-hint">
          <strong>Demo accounts</strong>
          <small>linh@acme.test · Manager</small>
          <small>minh@acme.test · Member</small>
          <small>ha@acme.test · Admin</small>
          <small>Password: password</small>
        </div>
      </div>
    </div>
  )
}
