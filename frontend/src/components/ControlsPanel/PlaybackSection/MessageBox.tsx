import type { ReactNode } from 'react'
import styles from './MessageBox.module.css'

type MessageBoxProps = {
  tone: 'step' | 'info' | 'success' | 'error'
  live?: boolean // announce changes to screen readers
  children: ReactNode
}

export function MessageBox({ tone, live = false, children }: MessageBoxProps) {
  return (
    <div className={`${styles.box} ${styles[tone]}`} role={live ? 'status' : undefined}>
      <span className={styles.key} aria-hidden="true" />
      {children}
    </div>
  )
}
