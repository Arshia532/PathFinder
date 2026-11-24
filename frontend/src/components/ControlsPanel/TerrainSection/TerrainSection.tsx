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

type TerrainSectionProps = {
    hidden?: boolean
}

export function TerrainSection({ hidden }: TerrainSectionProps) {
    return (
        <section
            id="panel-terrain"
            className={styles.section}
            role="tabpanel"
            aria-labelledby="tab-terrain"
            hidden={hidden}
        >
            <div className={styles.intro}>
                <p className={styles.lead}>Paint cells with different ground and height.</p>
                <Tag>
                    <LockIcon size={12} strokeWidth={2.2} />
                    Coming soon
                </Tag>
            </div>

            <fieldset className={styles.locked} disabled aria-describedby="terrain-note">
                <legend className="visually-hidden">Terrain settings, not available yet</legend>
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

            <p id="terrain-note" className={styles.note}>
                Weighted cells: moving costs more on rough ground and slopes.
            </p>
        </section>
    )
}