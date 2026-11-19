import styles from './StatsCards.module.css'
import { StatCard } from './StatCard'

type StatsCardsProps = {
    visited: number
    frontier: number
    openCells: number
    pathLength: number | null
}

export function StatsCards({ visited, frontier, openCells, pathLength }: StatsCardsProps) {
    return (
        <section className={styles.row} aria-label="Run statistics">
            <StatCard label="Nodes visited" value={String(visited)} note="Total nodes visited" tone="visited" />
            <StatCard label="Path length" value={pathLength === null ? '—' : String(pathLength)}
                note="Moves from start to destination" tone="path" />
            <StatCard label="Frontier nodes" value={String(frontier)} note="Waiting to be explored" tone="frontier" />
            <StatCard label="Coverage" value={`${Math.round(100 * visited / openCells)}%`}
                note={`${visited} of ${openCells} open cells explored`} tone="coverage" />
        </section>
    )
}
