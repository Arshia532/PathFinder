import type { AlgorithmId } from '../../api/types'
import { algorithms } from '../../data/algorithms'
import styles from './AlgorithmCard.module.css'

type AlgorithmCardProps = {
    algorithm: AlgorithmId
    bidirectional: boolean
}

export function AlgorithmCard({ algorithm: algorithmId, bidirectional }: AlgorithmCardProps) {
    const algorithm = algorithms.find((a) => a.id === algorithmId)
    if (!algorithm) return null

    return (
        <section className={styles.card} aria-labelledby="algorithm-card-title">
            <h2 id="algorithm-card-title" className={styles.title}>
                {algorithm.name}
            </h2>
            <p className={styles.text}>{algorithm.description}</p>

            {bidirectional && (
                <p className={styles.note}>
                Bi-directional: a second search starts from the end, and both stop where they meet.
                </p>
            )}
        </section>
    )
}