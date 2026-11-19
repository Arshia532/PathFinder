import { GRID_COLS, GRID_ROWS, placeholderSeed, placeholderTool } from '../../../data/placeholder';
import { GRID_MAX_SIZE, GRID_MIN_SIZE, gridSizePresets } from '../../../data/gridPresets';
import { Field } from '../../ui/Field';
//import { SectionTitle } from '../../ui/SectionTitle';
import { ChevronDownIcon } from '../../icons/ChevronDownIcon';
import { Stepper } from '../../ui/Stepper';
import { Button } from '../../ui/Button';
import { RotateCcwIcon } from '../../icons/RotateCcwIcon';
import { ShuffleIcon } from '../../icons/ShuffleIcon'; 

import { SegmentedControl, type SegmentOption } from '../../ui/SegmentedControl';

import styles from './MapSection.module.css'

const toolOptions: SegmentOption[] = [
    { value: 'start', label: 'Start', keyColor: 'var(--color-cell-start)' },
    { value: 'end', label: 'End', keyColor: 'var(--color-cell-end)' },
    { value: 'wall', label: 'Wall', keyColor: 'var(--color-cell-wall)' },
    { value: 'erase', label: 'Erase', keyColor: 'var(--color-cell-empty)', keyOutline: true },
]

type MapSectionProps = {
    hidden?: boolean
}

export function MapSection ({ hidden }: MapSectionProps) {
    return (
        <section
        id='panel-map'
        className={styles.section}
        role='tabpanel'
        aria-labelledby="tab-map"
        hidden={hidden}
        >
            <div className={styles.fields}>
                <Field label="Seed" htmlFor="seed">
                    <input className={`${styles.input} ${styles.mono}`}  id='seed' type='text' inputMode='numeric' defaultValue={placeholderSeed} />
                </Field>
                
                <Field label='Grid size' htmlFor='grid-size'>
                    <div className={styles.selectWrap}>
                        <select className={`${styles.input} ${styles.select}`} id="grid-size" defaultValue={`${GRID_COLS}x${GRID_ROWS}`}>
                            {gridSizePresets.map((p) => (
                                <option key={`${p.cols}x${p.rows}`} value={`${p.cols}x${p.rows}`}>
                                    {p.cols} x {p.rows}
                                </option>
                            ))}
                            <option value="custom" disabled>
                                Custom...
                            </option>
                        </select>
                        <ChevronDownIcon className={styles.chevron} />
                    </div>
                </Field>
            </div>
            <div>
                <div className={styles.fields}>
                    <Field label='Columns' labelId='cols-label'>
                        <Stepper 
                            value={GRID_COLS}
                            min={GRID_MIN_SIZE}
                            max={GRID_MAX_SIZE}
                            labelId='cols-label'
                            decreaseLabel='Fewer columns'
                            increaseLabel='More columns'
                        />
                    </Field>

                    <Field label='Rows' labelId='rows-label'>
                        <Stepper 
                            value={GRID_ROWS}
                            min={GRID_MIN_SIZE}
                            max={GRID_MAX_SIZE}
                            labelId='rows-label'
                            decreaseLabel='Fewer rows'
                            increaseLabel='More rows'
                        />
                    </Field>
                </div>
                <p className={styles.hint}> 
                    Smallest {GRID_MIN_SIZE} × {GRID_MIN_SIZE} · largest {GRID_MAX_SIZE} × {GRID_MAX_SIZE}
                </p>
            </div>
            <Field label='Customize map' labelId='tool-label'>
                <SegmentedControl options={toolOptions} selected={placeholderTool} labelId='tool-label'/>
            </Field>
            <div className={styles.actions}>
                <Button variant="primary" className={styles.grow}>
                    <ShuffleIcon size={18} strokeWidth={1.8}/>
                    Generate Random
                </Button>
                <Button variant="ghost" className={styles.fill}>
                    <RotateCcwIcon size={18} strokeWidth={1.8}/>
                    Reset map
                </Button>
            </div>
        </section>
    );
}