import type { ButtonHTMLAttributes } from 'react'
import styles from './IconButton.module.css'

type IconButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
    label: string
    variant?: 'default' | 'primary'
}

export function IconButton({ label, variant = 'default', ...props }: IconButtonProps) {
    return <button {...props} type="button" className={`${styles.button} ${styles[variant]}`} aria-label={label} />
}
