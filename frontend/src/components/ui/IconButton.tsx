import type { ReactNode } from 'react'
import styles from './IconButton.module.css'

type IconButtonProps = {
  label: string // spoken name, since the button has no visible text
  variant?: 'default' | 'primary'
  children: ReactNode
}

export function IconButton({ label, variant = 'default', children }: IconButtonProps) {
  return (
    <button type="button" className={`${styles.button} ${styles[variant]}`} aria-label={label}>
      {children}
    </button>
  )
}
