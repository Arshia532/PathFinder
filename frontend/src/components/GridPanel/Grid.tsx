import { GRID_COLS, GRID_ROWS } from "../../data/placeholder";
import styles from './Grid.module.css'

const cellIndexes = Array.from({length: GRID_COLS * GRID_ROWS}, (_, i) => i);

export function Grid() {
    return (
        <div className={styles.grid}>
            {cellIndexes.map((i) => (
                <div key={i} className={styles.cell} />
            ))}
        </div>
    );
}