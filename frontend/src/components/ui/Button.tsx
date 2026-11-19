import type { ButtonHTMLAttributes } from 'react'
import styles from './Button.module.css'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
    variant?: 'primary' | 'ghost' | 'light'
}

export function Button({ variant = 'primary', className = '', type = 'button', ...props }: ButtonProps) {
    return <button {...props} type={type} className={`${styles.button} ${styles[variant]} ${className}`} />
}
