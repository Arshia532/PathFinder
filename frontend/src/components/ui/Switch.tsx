import styles from './Switch.module.css'

type SwitchProps = {
    id: string
    label: string
    deafultChekced?: boolean
}

export function Switch({id, label, deafultChekced = false}: SwitchProps) {
    return (
        <label htmlFor={id} className={styles.row}>
            {label}
            <input 
                id={id}
                className={styles.switch}
                type='checkbox'
                role='switch'
                defaultChecked={deafultChekced}
            />
        </label>
    )
}