import type { ReactNode } from 'react'
import type { Cell, GridMap } from '../../api/types'
import styles from './Grid.module.css'
import { cellKey, type CellMark } from '../../state/searchView'

type GridProps = {
    map: GridMap
    onPaintStart: (cell: Cell) => void
    onPaintMove: (cell: Cell) => void
    marks: Map<string, CellMark> | null
    stale: boolean
}

const keyOf = (row: number, column: number) => cellKey({ row, column })

export function Grid({ map, marks, stale, onPaintStart, onPaintMove }: GridProps) {
    const wallKeys = new Set(map.walls.map((wall) => keyOf(wall.row, wall.column)))

    function kindClass(row: number, column: number) {
        if (row === map.start.row && column === map.start.column) return styles.start
        if (row === map.end.row && column === map.end.column) return styles.end
        if (wallKeys.has(keyOf(row, column))) return styles.wall
        const mark = marks?.get(keyOf(row, column))
        return mark ? styles[mark] : ''
    }

    const cells: ReactNode[] = []
    for (let row = 0; row < map.height; row++) {
        for (let column = 0; column < map.width; column++) {
            cells.push(
                <div
                    key={keyOf(row, column)}
                    className={`${styles.cell} ${kindClass(row, column)}`}
                    onPointerDown={(e) => {
                        if (e.button !== 0) return
                        if (e.currentTarget.hasPointerCapture(e.pointerId)) {
                            e.currentTarget.releasePointerCapture(e.pointerId)
                        }
                        onPaintStart({ row, column })
                    }}
                    onPointerEnter={(e) => {
                        if (e.buttons !== 1) return 
                        onPaintMove({ row, column })
                    }}
                />
            )
        }
    }

    return <div className={`${styles.grid} ${stale ? styles.stale : ''}`}>{cells}</div>
}