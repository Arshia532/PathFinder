import type { CSSProperties } from "react";
import type { Cell, GridMap } from "../../api/types";
import { Grid } from "./Grid";
import styles from './GridPanel.module.css'
import { Legend } from './Legend'
import type { CellMark } from "../../state/searchView";

type GridPanelProps = {
    map: GridMap
    onPaintStart: (cell: Cell) => void
    onPaintMove: (cell: Cell) => void
    marks: Map<string, CellMark> | null
    stale: boolean
    pathOrder: Map<string, number> | null
    animatePath: boolean
}

export function GridPanel ({ map, onPaintStart, onPaintMove, marks, stale, pathOrder, animatePath }: GridPanelProps) {
    const sizeVars = {'--cols': map.width} as CSSProperties;

    return (
        <section className={styles.panel} style={sizeVars} aria-label="Search grid">
            <Grid 
                map={map}
                marks={marks}
                pathOrder={pathOrder}
                animatePath={animatePath}
                stale={stale}
                onPaintStart={onPaintStart}
                onPaintMove={onPaintMove}
            />
            <div className={styles.footer}>
                <Legend />
                <p className={styles.hint}>Click or drag on the grid to use the selected tool</p>
            </div>
        </section>
    );
}