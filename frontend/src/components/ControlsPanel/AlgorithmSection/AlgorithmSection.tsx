import type { AlgorithmId } from '../../../api/types'
import { algorithms } from '../../../data/algorithms'
import { SectionTitle } from '../../ui/SectionTitle'
import { SegmentedControl, type SegmentOption } from '../../ui/SegmentedControl'
import styles from './AlgorithmSection.module.css'

const algorithmOptions: SegmentOption<AlgorithmId>[] = algorithms.map(({ id, label }) => ({
    value: id,
    label,
    disabled: id !== 'bfs' && id !== 'dfs',
}))

type AlgorithmSectionProps = {
    algorithm: AlgorithmId
    onAlgorithmChange: (algorithm: AlgorithmId) => void
    disabled: boolean
}

export function AlgorithmSection({ algorithm, onAlgorithmChange, disabled }: AlgorithmSectionProps) {
    return (
        <section className={styles.section} aria-labelledby="algorithm-title">
            <SectionTitle id="algorithm-title" dotColor="var(--color-dot-algorithm)">Algorithm</SectionTitle>
            <SegmentedControl
                options={algorithmOptions}
                selected={algorithm}
                onSelect={onAlgorithmChange}
                disabled={disabled}
                labelId="algorithm-title"
                tone="accent"
            />
        </section>
    )
}
