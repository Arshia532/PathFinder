import type { SearchResponse } from '../../../api/types'
import { ChevronLeftIcon } from '../../icons/ChevronLeftIcon'
import { ChevronRightIcon } from '../../icons/ChevronRightIcon'
import { PlayIcon } from '../../icons/PlayIcon'
import { SkipBackIcon } from '../../icons/SkipBackIcon'
import { SkipForwardIcon } from '../../icons/SkipForwardIcon'
import { Button } from '../../ui/Button'
import { IconButton } from '../../ui/IconButton'
import { SectionTitle } from '../../ui/SectionTitle'
import { MessageBox } from './MessageBox'
import styles from './PlaybackSection.module.css'

export type PlaybackSectionProps = {
    busy: 'generate' | 'search' | null
    error: string
    cursor: number
    result: SearchResponse | null
    playing: boolean
    onSearch: () => void
    onSeek: (step: number) => void
    onTogglePlay: () => void
}

export function PlaybackSection({
    busy, error, cursor, result, playing, onSearch, onSeek, onTogglePlay,
}: PlaybackSectionProps) {
    const total = result?.steps.length ?? 0
    const step = result?.steps[cursor - 1]
    return (
        <section className={styles.section} aria-labelledby="playback-title">
            <SectionTitle id="playback-title" dotColor="var(--color-dot-playback)">Playback</SectionTitle>
            <Button disabled={busy !== null} onClick={onSearch}>Run search</Button>
            <div className={styles.controls}>
                <IconButton label="Jump to first step" disabled={busy !== null || cursor === 0} onClick={() => onSeek(0)}>
                    <SkipBackIcon size={18} strokeWidth={1.8} />
                </IconButton>
                <IconButton label="Previous step" disabled={busy !== null || cursor === 0} onClick={() => onSeek(cursor - 1)}>
                    <ChevronLeftIcon size={18} />
                </IconButton>
                <IconButton label={playing ? 'Pause' : 'Play'} variant="primary" disabled={busy !== null || total === 0}
                    onClick={onTogglePlay}>
                    {playing ? <span aria-hidden="true">❚❚</span> : <PlayIcon size={22} className={styles.playIcon} />}
                </IconButton>
                <IconButton label="Next step" disabled={busy !== null || cursor === total} onClick={() => onSeek(cursor + 1)}>
                    <ChevronRightIcon size={18} />
                </IconButton>
                <IconButton label="Jump to last step" disabled={busy !== null || cursor === total} onClick={() => onSeek(total)}>
                    <SkipForwardIcon size={18} strokeWidth={1.8} />
                </IconButton>
            </div>
            {result && (
                <MessageBox tone="step">
                    {step ? <span>Expanded ({step.expanded.row}, {step.expanded.column})</span> : <span>Ready to play</span>}
                    <span className={styles.counter}>Step {cursor} / {total}</span>
                </MessageBox>
            )}
            {busy && <MessageBox tone="info" live>{busy === 'generate' ? 'Generating map…' : 'Running search…'}</MessageBox>}
            {error && <MessageBox tone="error" live>{error}</MessageBox>}
            {!busy && !error && result && cursor === total && (
                <MessageBox tone={result.found ? 'success' : 'error'} live>
                    {result.found ? `Path found in ${result.stats.pathLength} moves.` : 'No path to the destination.'}
                </MessageBox>
            )}
        </section>
    )
}
