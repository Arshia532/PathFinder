import styles from './StatCard.module.css'

type StatCardProps = {
    label: string
    value: string
    note: string
    tone: 'visited' | 'path' | 'frontier' | 'coverage'
}

export function StatCard({label, value, note, tone}: StatCardProps) {
    return (
        <div className={`${styles.card} ${styles[tone]}`}>
            <p className={styles.label}>{label}</p>
            <p className={styles.value}>{value}</p>
            <p className={styles.note}>{note}</p>
        </div>
    )
}