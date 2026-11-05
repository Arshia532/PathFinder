import { SectionTitle } from "../../ui/SectionTitle";

import { ChevronDownIcon } from '../../icons/ChevronDownIcon'
import { LockIcon } from '../../icons/LockIcon'
import { Tag } from '../../ui/Tag'

import { Field } from "../../ui/Field";
import { SegmentedControl, type SegmentOption } from "../../ui/SegmentedControl";

import styles from './TerrainSection.module.css'

const terrainOptions: SegmentOption[] = [
    { value: 'grass', label: 'Grass', keyColor: 'var(--color-terrain-grass)' },
    { value: 'mud', label: 'Mud', keyColor: 'var(--color-terrain-mud)' },
    { value: 'stone', label: 'Stone', keyColor: 'var(--color-terrain-stone)' },
    { value: 'pavement', label: 'Pavement', keyColor: 'var(--color-terrain-pavement)' },
]


export function TerrainSection() {
    return (
        <section className={styles.section} aria-labelledby="terrain-title">
            <details className={styles.details}>
                <summary className={styles.summary}>
                    <SectionTitle id="terrain-title" dotColor="var(--color-dot-terrain)">
                        Terrain
                    </SectionTitle>
                    <span className={styles.meta}>
                        <Tag>
                            <LockIcon size={12} strokeWidth={2.2}/>
                            Coming soon
                        </Tag>
                        <span className={styles.chevron}>
                            <ChevronDownIcon />                        
                        </span>
                    </span>
                </summary>
                <div className={styles.body}>
                    <fieldset className={styles.locked} disabled aria-describedby="terrain-note">
                        <legend className="visually-hidden">Terrain settings, not avaiable yet</legend>
                        <Field label="Type" labelId="terrain-type-label">
                            <SegmentedControl options={terrainOptions} labelId="terrain-type-label" />
                        </Field>
                        <Field
                        label="Height"
                        htmlFor="terrain-height"
                        trailing={<span className={styles.value}>0</span>}
                        >
                            <input
                            id="terrain-height"
                            className={styles.slider}
                            type="range"
                            min={0}
                            max={5}
                            defaultValue={0}
                            />
                            <div className={styles.scale}>
                                <span>Flat</span>
                                <span>Steep</span>
                            </div>
                        </Field>
                    </fieldset>
                    <p className={styles.note}>
                        Weighted cells: moving costs more on rough ground and slopes.
                    </p>
                </div>
            </details>
        </section>
    )
}