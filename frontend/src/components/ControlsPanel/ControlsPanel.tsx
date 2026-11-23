import styles from './ControlsPanel.module.css'
import { ControlsTabs } from './ControlsTabs/ControlsTabs';
import { MapSection } from './MapSection/MapSection';
import { TerrainSection } from './TerrainSection/TerrainSection';
import { AlgorithmSection } from './AlgorithmSection/AlgorithmSection';
import { PlaybackSection } from './PlaybackSection/PlaybackSection';
import type { AlgorithmId } from '../../api/types';
import type { Tool } from '../../state/appState';
import type { ResultMessage, StepInfo } from '../../state/searchView';
import type { Playback } from '../../hooks/usePlayback';

import { useState } from 'react';

type ControlsPanelProps = {
    algorithm: AlgorithmId
    bidirectional: boolean
    onAlgorithmChange: (algorithm: AlgorithmId) => void
    onBidirectionalChange: (bidirectional: boolean) => void
    tool: Tool
    onToolChange: (tool: Tool) => void
    onResetMap: () => void
    step: StepInfo
    result: ResultMessage
    playback: Playback
    width: number
    height: number
    onResize: (width: number, height: number) => void
    seed: string
    onSeedChange: (seed: string) => void
    onGenerateRandom: () => void
    onGenerateFromSeed: () => void
}

export function ControlsPanel ({ algorithm, bidirectional, onAlgorithmChange, onBidirectionalChange, tool, onToolChange, onResetMap,
            step, result, playback, width, height, onResize, seed, onSeedChange, onGenerateRandom, onGenerateFromSeed  }: ControlsPanelProps) {
    const [activeTab, setActiveTab] = useState('map');

    return (
        <aside className={styles.panel} aria-label='Controls'>
            <header className={styles.header}>
                <h2 className={styles.title}>Controls</h2>
            </header>

            <ControlsTabs selected={activeTab} onSelect={setActiveTab} />
            <MapSection
                hidden={activeTab !== 'map'}
                tool={tool}
                onToolChange={onToolChange}
                onResetMap={onResetMap}
                width={width}
                height={height}
                onResize={onResize}
                seed={seed}
                onSeedChange={onSeedChange}
                onGenerateRandom={onGenerateRandom}
                onGenerateFromSeed={onGenerateFromSeed}
            />
            <TerrainSection hidden={activeTab !== 'terrain'} />
            <AlgorithmSection 
                algorithm = {algorithm}
                bidirectional = {bidirectional}
                onAlgorithmChange = {onAlgorithmChange}
                onBidirectionalChange = {onBidirectionalChange}
            />
            <PlaybackSection step={step} result={result} playback={playback} />
        </aside>
    );
}