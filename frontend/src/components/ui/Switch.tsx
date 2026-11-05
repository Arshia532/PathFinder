import styles from './Switch.module.css'

type SwitchProps = {
    id: string
    label: string
    defaultChecked?: boolean
}

export function Switch({id, label, defaultChecked = false}: SwitchProps) {
    return (
        <label htmlFor={id} className={styles.row}>
            {label}
            <input 
                id={id}
                className={styles.switch}
                type='checkbox'
                role='switch'
                defaultChecked={defaultChecked}
            />
        </label>
    )
}