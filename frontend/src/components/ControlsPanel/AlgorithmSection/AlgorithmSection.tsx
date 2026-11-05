import { SectionTitle } from "../../ui/SectionTitle";

import { algorithms } from '../../../data/algorithms'
import { placeholderAlgorithm, placeholderBidirection } from '../../../data/placeholder'
import { SegmentedControl, type SegmentOption } from '../../ui/SegmentedControl'

import { Switch } from '../../ui/Switch'

import styles from './AlgorithmSection.module.css';

const algorithmOptions: SegmentOption[] = algorithms.map((a) => ({
    value: a.id,
    label: a.label
}))

export function AlgorithmSection() {
    return (
        <section className={styles.section} aria-labelledby="algorithm-title">
            <SectionTitle id="algorithm-title" dotColor="var(--color-dot-algorithm)">
                Algorithm
            </SectionTitle>
            
            <SegmentedControl
                options={algorithmOptions}
                selected={placeholderAlgorithm}
                labelId="algorithm-title"
                tone="accent"
            />

            <Switch id="bidirectional" label="Bi-directional" defaultChecked={placeholderBidirection}/>
        </section>
    )
}