import type { ReactNode } from 'react'
import styles from './Button.module.css'

type ButtonProps = {
    variant?: 'primary' | 'ghost' | 'light'
    className?: string
    children: ReactNode
    onClick?: () => void
}

export function Button ({variant = 'primary', className = '', onClick, children}: ButtonProps) {
    return (
        <button type='button' className={`${styles.button} ${styles[variant]} ${className}`} onClick={onClick}>
            {children}
        </button>
    )
}