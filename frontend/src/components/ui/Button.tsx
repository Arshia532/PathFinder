import type { ReactNode } from 'react'
import styles from './Button.module.css'

type ButtonProps = {
    variant?: 'primary' | 'ghost'
    className?: string
    children: ReactNode
}

export function Button ({variant = 'primary', className = '', children}: ButtonProps) {
    return (
        <button type='button' className={`${styles.button} ${styles[variant]} ${className}`}>
            {children}
        </button>
    )
}