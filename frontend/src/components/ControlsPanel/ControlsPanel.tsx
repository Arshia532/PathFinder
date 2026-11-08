import styles from './ControlsPanel.module.css'
import { ControlsTabs } from './ControlsTabs/ControlsTabs';
import { MapSection } from './MapSection/MapSection';
import { TerrainSection } from './TerrainSection/TerrainSection';
import { AlgorithmSection } from './AlgorithmSection/AlgorithmSection';
import { PlaybackSection } from './PlaybackSection/PlaybackSection';

import { useState } from 'react';

export function ControlsPanel () {
    const [activeTab, setActiveTab] = useState('map');

    return (
        <aside className={styles.panel} aria-label='Controls'>
            <header className={styles.header}>
                <h2 className={styles.title}>Controls</h2>
            </header>

            <ControlsTabs selected={activeTab} onSelect={setActiveTab} />
            <MapSection hidden={activeTab !== 'map'} />
            <TerrainSection hidden={activeTab !== 'terrain'} />
            <AlgorithmSection />
            <PlaybackSection />
        </aside>
    );
}