import { placeholderStats } from "../../data/placeholder";
import styles from "./StatsCards.module.css";
import { StatCard } from "./StatCard";

export function StatsCards() {
    return (
        <section className={styles.row} aria-label="Run statistics">
            <StatCard
                label="Nodes visited"
                value={placeholderStats.visited}
                note="Total nodes visited"
                tone="visited"
            />
            <StatCard
                label="Path found"
                value={placeholderStats.pathLength}
                note="Shown when the search ends"
                tone="path"
            />
            <StatCard
                label="Frontier nodes"
                value={placeholderStats.frontier}
                note="Waiting to be explored"
                tone="frontier"
            />
            <StatCard
                label="Coverage"
                value={placeholderStats.coverage}
                note={`${placeholderStats.visited} of ${placeholderStats.openCells} open cells explored`}
                tone="coverage"
            />
        </section>
    );
}
