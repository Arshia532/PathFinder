import type { CSSProperties } from "react";
import { GRID_COLS } from "../../data/placeholder";
import { Grid } from "./Grid";
import styles from './GridPanel.module.css'
import { Legend } from './Legend'

const sizeVars = {'--cols': GRID_COLS} as CSSProperties;

export function GridPanel () {
    return (
        <section className={styles.panel} style={sizeVars} aria-label="Search grid">
            <Grid />
            <div className={styles.footer}>
                <Legend />
                <p className={styles.hint}>Click or drag on the grid to use the selected tool</p>
            </div>
        </section>
    );
}