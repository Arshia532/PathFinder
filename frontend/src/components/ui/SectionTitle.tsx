import type { ReactNode } from 'react'
import styles from './SectionTitle.module.css'

type SectionTitleProps = {
    id: string
    dotColor: string
    children: ReactNode
}

export function SectionTitle ({id, dotColor, children}: SectionTitleProps) {
    return (
        <h3 id={id} className={styles.title}>
            <span className={styles.dot} style={{background: dotColor}} aria-hidden="true"></span>
            {children}
        </h3>
    )
}