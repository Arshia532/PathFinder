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

type PlaybackSectionProps = {
    step: StepInfo
    result: ResultMessage
}

export function PlaybackSection({ step, result }: PlaybackSectionProps) {
    return (
        <section className={styles.section} aria-labelledby="playback-title">
            <SectionTitle id="playback-title" dotColor="var(--color-dot-playback)">
                Playback
            </SectionTitle>

            <div className={styles.controls}>
                <IconButton label="Jump to first step">
                    <SkipBackIcon size={18} strokeWidth={1.8} />
                </IconButton>
                <IconButton label="Previous step">
                    <ChevronLeftIcon size={18} />
                </IconButton>
                <IconButton label="Play" variant="primary">
                    <PlayIcon size={22} className={styles.playIcon} />
                </IconButton>
                <IconButton label="Next step">
                    <ChevronRightIcon size={18} />
                </IconButton>
                <IconButton label="Jump to last step">
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
