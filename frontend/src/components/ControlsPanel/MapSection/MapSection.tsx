import { useState } from 'react'
import type { GenerateMode } from '../../../api/client'
import { GRID_MAX_SIZE, GRID_MIN_SIZE, gridSizePresets } from '../../../data/gridPresets'
import type { Tool } from '../../../state/appState'
import { Button } from '../../ui/Button'
import { Field } from '../../ui/Field'
import { SegmentedControl, type SegmentOption } from '../../ui/SegmentedControl'
import { Stepper } from '../../ui/Stepper'
import { ChevronDownIcon } from '../../icons/ChevronDownIcon'
import { RotateCcwIcon } from '../../icons/RotateCcwIcon'
import { ShuffleIcon } from '../../icons/ShuffleIcon'
import styles from './MapSection.module.css'

const toolOptions: SegmentOption<Tool>[] = [
    { value: 'start', label: 'Start', keyColor: 'var(--color-cell-start)' },
    { value: 'end', label: 'End', keyColor: 'var(--color-cell-end)' },
    { value: 'wall', label: 'Wall', keyColor: 'var(--color-cell-wall)' },
    { value: 'erase', label: 'Erase', keyColor: 'var(--color-cell-empty)', keyOutline: true },
]

export type MapSectionProps = {
    hidden?: boolean
    tool: Tool
    busy: boolean
    onToolChange: (tool: Tool) => void
    onGenerate: (width: number, height: number, seed: string, mode: GenerateMode) => void
    onReset: (width: number, height: number) => void
}

export function MapSection({ hidden, tool, busy, onToolChange, onGenerate, onReset }: MapSectionProps) {
    const [seed, setSeed] = useState('485161065432132')
    const [width, setWidth] = useState(30)
    const [height, setHeight] = useState(21)
    const preset = gridSizePresets.some(size => size.cols === width && size.rows === height)

    return (
        <section id="panel-map" className={styles.section} role="tabpanel" aria-labelledby="tab-map" hidden={hidden}>
            <div className={styles.fields}>
                <Field label="Seed" htmlFor="seed">
                    <input className={`${styles.input} ${styles.mono}`} id="seed" type="text"
                        value={seed} disabled={busy} onChange={event => setSeed(event.target.value)} />
                </Field>
                <Field label="Grid size" htmlFor="grid-size">
                    <div className={styles.selectWrap}>
                        <select className={`${styles.input} ${styles.select}`} id="grid-size" disabled={busy}
                            value={preset ? `${width}x${height}` : 'custom'}
                            onChange={event => {
                                const [columns, rows] = event.target.value.split('x').map(Number)
                                setWidth(columns)
                                setHeight(rows)
                            }}>
                            {gridSizePresets.map(({ cols, rows }) => (
                                <option key={`${cols}x${rows}`} value={`${cols}x${rows}`}>{cols} × {rows}</option>
                            ))}
                            <option value="custom" disabled>Custom</option>
                        </select>
                        <ChevronDownIcon className={styles.chevron} />
                    </div>
                </Field>
            </div>
            <div className={styles.fields}>
                <Field label="Columns" labelId="cols-label">
                    <Stepper value={width} min={GRID_MIN_SIZE} max={GRID_MAX_SIZE} disabled={busy}
                        onChange={setWidth} labelId="cols-label" decreaseLabel="Fewer columns" increaseLabel="More columns" />
                </Field>
                <Field label="Rows" labelId="rows-label">
                    <Stepper value={height} min={GRID_MIN_SIZE} max={GRID_MAX_SIZE} disabled={busy}
                        onChange={setHeight} labelId="rows-label" decreaseLabel="Fewer rows" increaseLabel="More rows" />
                </Field>
            </div>
            <Field label="Customize map" labelId="tool-label">
                <SegmentedControl options={toolOptions} selected={tool} onSelect={onToolChange} disabled={busy}
                    labelId="tool-label" />
            </Field>
            <div className={styles.actions}>
                <Button disabled={busy} className={styles.grow} onClick={() => onGenerate(width, height, seed, 'maze')}>
                    <ShuffleIcon size={18} strokeWidth={1.8} />
                    Generate Maze
                </Button>
                <Button variant="ghost" disabled={busy} className={styles.fill}
                    onClick={() => onGenerate(width, height, seed, 'random')}>
                    Generate Random
                </Button>
                <Button variant="ghost" disabled={busy} className={styles.fill} onClick={() => onReset(width, height)}>
                    <RotateCcwIcon size={18} strokeWidth={1.8} />
                    Reset map
                </Button>
            </div>
        </section>
    )
}
