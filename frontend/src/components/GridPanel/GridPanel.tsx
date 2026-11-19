import type { ComponentProps } from 'react'
import { Grid } from './Grid'
import { Legend } from './Legend'
import styles from './GridPanel.module.css'

export function GridPanel(props: ComponentProps<typeof Grid>) {
    return (
        <section className={styles.panel} aria-label="Search grid">
            <Grid {...props} />
            <div className={styles.footer}>
                <Legend />
                <p className={styles.hint}>Click or drag on the grid to use the selected tool</p>
            </div>
        </section>
    )
}
