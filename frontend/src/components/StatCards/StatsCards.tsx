import styles from "./StatsCards.module.css";
import { StatCard } from "./StatCard";

type StatsCardsProps = {
    visited: number
    frontier: number
    pathLength: number | null
    openCells: number
}

export function StatsCards({ visited, frontier, pathLength, openCells }: StatsCardsProps) {
    const coverage = openCells > 0 ? Math.round((visited / openCells) * 100) : 0

    return (
        <section className={styles.row} aria-label="Run statistics">
            <StatCard
                label="Nodes visited"
                value={String(visited)}
                note="Total nodes visited"
                tone="visited"
            />
            <StatCard
                label="Path found"
                value={pathLength === null ? '-' : String(pathLength)}
                note="Shown when the search ends"
                tone="path"
            />
            <StatCard
                label="Frontier nodes"
                value={String(frontier)}
                note="Waiting to be explored"
                tone="frontier"
            />
            <StatCard
                label="Coverage"
                value={`${coverage}%`}
                note={`${visited} of ${openCells} open cells explored`}
                tone="coverage"
            />
        </section>
    );
}