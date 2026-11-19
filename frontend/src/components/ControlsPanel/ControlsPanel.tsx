import styles from './ControlsPanel.module.css'
import { ControlsTabs } from './ControlsTabs/ControlsTabs';
import { MapSection } from './MapSection/MapSection';
import { TerrainSection } from './TerrainSection/TerrainSection';
import { AlgorithmSection } from './AlgorithmSection/AlgorithmSection';
import { PlaybackSection } from './PlaybackSection/PlaybackSection';
import type { AlgorithmId } from '../../api/types';

import { useState } from 'react';

type ControlsPanelProps = {
    algorithm: AlgorithmId
    bidirectional: boolean
    onAlgorithmChange: (algorithm: AlgorithmId) => void
    onBidirectionalChange: (bidirectional: boolean) => void
}

export function ControlsPanel ({ algorithm, bidirectional, onAlgorithmChange, onBidirectionalChange }: ControlsPanelProps) {
    const [activeTab, setActiveTab] = useState('map');

    return (
        <aside className={styles.panel} aria-label='Controls'>
            <header className={styles.header}>
                <h2 className={styles.title}>Controls</h2>
            </header>

            <ControlsTabs selected={activeTab} onSelect={setActiveTab} />
            <MapSection hidden={activeTab !== 'map'} />
            <TerrainSection hidden={activeTab !== 'terrain'} />
            <AlgorithmSection 
                algorithm = {algorithm}
                bidirectional = {bidirectional}
                onAlgorithmChange = {onAlgorithmChange}
                onBidirectionalChange = {onBidirectionalChange}
            />
            <PlaybackSection />
        </aside>
    );
}