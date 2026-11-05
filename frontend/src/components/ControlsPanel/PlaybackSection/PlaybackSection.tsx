import { placeholderResult, placeholderStep } from '../../../data/placeholder'
import { ChevronLeftIcon } from '../../icons/ChevronLeftIcon'
import { ChevronRightIcon } from '../../icons/ChevronRightIcon'
import { PlayIcon } from '../../icons/PlayIcon'
import { SkipBackIcon } from '../../icons/SkipBackIcon'
import { SkipForwardIcon } from '../../icons/SkipForwardIcon'
import { IconButton } from '../../ui/IconButton'
import { SectionTitle } from "../../ui/SectionTitle";
import { MessageBox } from './MessageBox'
import styles from './PlaybackSection.module.css'

export function PlaybackSection() {
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
                    {placeholderStep.verb}{' '}
                    <strong className={styles.coords}>
                        ({placeholderStep.x}, {placeholderStep.y})
                    </strong>
                </span>
                <span className={styles.counter}>
                    step {placeholderStep.current} / {placeholderStep.total}
                </span>
            </MessageBox>

            <MessageBox tone={placeholderResult.tone} live>
                {placeholderResult.text}
            </MessageBox>
        </section>
    )
}