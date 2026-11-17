import styles from './Switch.module.css'

type SwitchProps = {
    id: string
    label: string
    checked?: boolean
    onChange: (checked: boolean) => void
}

export function Switch({id, label, checked, onChange}: SwitchProps) {
    return (
        <label htmlFor={id} className={styles.row}>
            {label}
            <input 
                id={id}
                className={styles.switch}
                type='checkbox'
                role='switch'
                checked={checked}
                onChange={(e) => onChange(e.target.checked)}
            />
        </label>
    )
}