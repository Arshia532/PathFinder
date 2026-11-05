import styles from './ControlsPanel.module.css'
import { MapSection } from './MapSection/MapSection';
import { TerrainSection } from './TerrainSection/TerrainSection';

export function ControlsPanel () {
    return (
        <aside className={styles.panel} aria-label='Controls'>
            <header className={styles.header}>
                <h2 className={styles.title}>Controls</h2>
            </header>
            
            <MapSection />
            <TerrainSection />
            <div className={styles.placeholder}>Algorithm</div>
            <div className={styles.placeholder}>Playback</div>
        </aside>

    );
}