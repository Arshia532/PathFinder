import { SectionTitle } from "../../ui/SectionTitle";

import { algorithms } from '../../../data/algorithms'
import { SegmentedControl, type SegmentOption } from '../../ui/SegmentedControl'

import { Switch } from '../../ui/Switch'

import styles from './AlgorithmSection.module.css';
import type { AlgorithmId } from "../../../api/types";

const algorithmOptions: SegmentOption<AlgorithmId>[] = algorithms.map((a) => ({
    value: a.id,
    label: a.label
}))

type AlgorithmSectionProps = {
    algorithm: AlgorithmId
    bidirectional: boolean
    onAlgorithmChange: (algorithm: AlgorithmId) => void
    onBidirectionalChange: (bidirectional: boolean) => void
}

export function AlgorithmSection( {algorithm, bidirectional, onAlgorithmChange, onBidirectionalChange }: AlgorithmSectionProps) {
    return (
        <section className={styles.section} aria-labelledby="algorithm-title">
            <SectionTitle id="algorithm-title" dotColor="var(--color-dot-algorithm)">
                Algorithm
            </SectionTitle>
            
            <SegmentedControl
                options={algorithmOptions}
                selected={algorithm}
                onSelect={onAlgorithmChange}
                labelId="algorithm-title"
                tone="accent"
            />

            <Switch 
                id="bidirectional" 
                label="Bi-directional" 
                checked={bidirectional}
                onChange={onBidirectionalChange}
            />
        </section>
    )
}