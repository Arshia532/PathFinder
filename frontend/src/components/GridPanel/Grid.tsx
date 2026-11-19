import type { CSSProperties, KeyboardEvent } from 'react'
import type { Cell, GridMap } from '../../api/types'
import { cellIndex, type playbackFrame } from '../../state/playback'
import styles from './Grid.module.css'

type GridProps = {
    map: GridMap
    frame: ReturnType<typeof playbackFrame>
    onEdit: (cell: Cell) => void
}

export function Grid({ map, frame, onEdit }: GridProps) {
    const walls = new Set(map.walls.map(cell => cellIndex(cell, map.width)))
    const start = cellIndex(map.start, map.width)
    const end = cellIndex(map.end, map.width)

    function moveFocus(event: KeyboardEvent<HTMLDivElement>) {
        const cell = event.target
        if (!(cell instanceof HTMLButtonElement)) return
        let row = Number(cell.dataset.row)
        let column = Number(cell.dataset.column)
        switch (event.key) {
            case 'ArrowUp': row--; break
            case 'ArrowDown': row++; break
            case 'ArrowLeft': column--; break
            case 'ArrowRight': column++; break
            default: return
        }
        event.preventDefault()
        if (row < 0 || row >= map.height || column < 0 || column >= map.width) return
        const next = event.currentTarget.children[row * map.width + column]
        if (next instanceof HTMLButtonElement) next.focus()
    }

    return (
        <div className={styles.grid} style={{ '--cols': map.width } as CSSProperties} onKeyDown={moveFocus}>
            {Array.from({ length: map.width * map.height }, (_, index) => {
                const row = Math.floor(index / map.width)
                const column = index % map.width
                const cell = { row, column }
                const kind = index === start ? 'start'
                    : index === end ? 'end'
                    : walls.has(index) ? 'wall'
                    : frame.path.has(index) ? 'path'
                    : frame.current === index ? 'current'
                    : frame.visited.has(index) ? 'visited'
                    : frame.frontier.has(index) ? 'frontier'
                    : 'empty'
                return (
                    <button key={index} type="button" className={styles.cell} data-kind={kind}
                        data-row={row} data-column={column} tabIndex={index === start ? 0 : -1}
                        aria-label={`Row ${row}, column ${column}, ${kind}`}
                        onClick={() => onEdit(cell)}
                        onPointerEnter={event => { if (event.buttons === 1) onEdit(cell) }} />
                )
            })}
        </div>
    )
}
