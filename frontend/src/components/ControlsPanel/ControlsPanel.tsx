import { useState } from 'react'
import type { AlgorithmId } from '../../api/types'
import { AlgorithmSection } from './AlgorithmSection/AlgorithmSection'
import { ControlsTabs } from './ControlsTabs/ControlsTabs'
import { MapSection, type MapSectionProps } from './MapSection/MapSection'
import { PlaybackSection, type PlaybackSectionProps } from './PlaybackSection/PlaybackSection'
import { TerrainSection } from './TerrainSection/TerrainSection'
import styles from './ControlsPanel.module.css'

type ControlsPanelProps = {
    mapControls: MapSectionProps
    algorithm: AlgorithmId
    onAlgorithmChange: (algorithm: AlgorithmId) => void
    playback: PlaybackSectionProps
}

export function ControlsPanel({ mapControls, algorithm, onAlgorithmChange, playback }: ControlsPanelProps) {
    const [activeTab, setActiveTab] = useState('map')
    return (
        <aside className={styles.panel} aria-label="Controls">
            <header className={styles.header}>
                <h2 className={styles.title}>Controls</h2>
            </header>
            <ControlsTabs selected={activeTab} onSelect={setActiveTab} />
            <MapSection {...mapControls} hidden={activeTab !== 'map'} />
            <TerrainSection hidden={activeTab !== 'terrain'} />
            <AlgorithmSection algorithm={algorithm} onAlgorithmChange={onAlgorithmChange} disabled={playback.busy !== null} />
            <PlaybackSection {...playback} />
        </aside>
    )
}
