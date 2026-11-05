import { algorithms } from '../../data/algorithms'
import { placeholderAlgorithm, placeholderBidirection } from '../../data/placeholder'
import styles from './AlgorithmCard.module.css'

export function AlgorithmCard() {
    const algorithm = algorithms.find((a) => a.id === placeholderAlgorithm)
    if (!algorithm) return null

    return (
        <section className={styles.card} aria-labelledby="algorithm-card-title">
            <h2 id="algorithm-card-title" className={styles.title}>
                {algorithm.name}
            </h2>
            <p className={styles.text}>{algorithm.description}</p>

            {placeholderBidirection && (
                <p className={styles.note}>
                Bi-directional: a second search starts from the end, and both stop where they meet.
                </p>
            )}
        </section>
    )
}