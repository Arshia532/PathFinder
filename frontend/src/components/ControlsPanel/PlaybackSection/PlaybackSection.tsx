import { ChevronLeftIcon } from '../../icons/ChevronLeftIcon'
import { ChevronRightIcon } from '../../icons/ChevronRightIcon'
import { PlayIcon } from '../../icons/PlayIcon'
import { SkipBackIcon } from '../../icons/SkipBackIcon'
import { SkipForwardIcon } from '../../icons/SkipForwardIcon'
import { IconButton } from '../../ui/IconButton'
import { SectionTitle } from "../../ui/SectionTitle";
import { MessageBox } from './MessageBox'
import styles from './PlaybackSection.module.css'
import type { ResultMessage, StepInfo } from '../../../state/searchView'
import { PauseIcon } from '../../icons/PauseIcon'
import type { Playback } from '../../../hooks/usePlayback'

type PlaybackSectionProps = {
    step: StepInfo
    result: ResultMessage
    playback: Playback
}

export function PlaybackSection({ step, result, playback }: PlaybackSectionProps) {
    const empty = playback.total === 0
    const atStart = playback.index === 0
    const atEnd = playback.index === playback.total
    return (
        <section className={styles.section} aria-labelledby="playback-title">
            <SectionTitle id="playback-title" dotColor="var(--color-dot-playback)">
                Playback
            </SectionTitle>
            <div className={styles.controls}>
                <IconButton label="Jump to first step" onClick={playback.first} disabled={empty || atStart}>
                    <SkipBackIcon size={18} strokeWidth={1.8} />
                </IconButton>
                <IconButton label="Previous step" onClick={playback.previous} disabled={empty || atStart}>
                    <ChevronLeftIcon size={18} />
                </IconButton>
                <IconButton
                    label={playback.playing ? 'Pause' : 'Play'}
                    variant="primary"
                    onClick={playback.togglePlay}
                    disabled={empty}
                >
                    {playback.playing
                        ? <PauseIcon size={20} />
                        : <PlayIcon size={22} className={styles.playIcon} />}
                </IconButton>
                <IconButton label="Next step" onClick={playback.next} disabled={empty || atEnd}>
                    <ChevronRightIcon size={18} />
                </IconButton>
                <IconButton label="Jump to last step" onClick={playback.last} disabled={empty || atEnd}>
                    <SkipForwardIcon size={18} strokeWidth={1.8} />
                </IconButton>
            </div>

            <MessageBox tone="step">
                <span>
                    {step.verb}{' '}
                    <strong className={styles.coords}>
                        ({step.cell.column}, {step.cell.row})
                    </strong>
                </span>
                <span className={styles.counter}>
                    step {step.index} / {step.total}
                </span>
            </MessageBox>

            <MessageBox tone={result.tone} live>
                {result.text}
            </MessageBox>
        </section>
    )
}
