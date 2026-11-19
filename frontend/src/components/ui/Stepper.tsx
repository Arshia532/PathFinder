import { MinusIcon } from "../icons/MinusIcon";
import { PlusIcon } from "../icons/PlusIcon";
import styles from './Stepper.module.css'

type StepperProps = {
    onChange: (value: number) => void
    disabled?: boolean
    value: number
    min: number
    max: number
    labelId: string
    decreaseLabel: string
    increaseLabel: string
}

export function Stepper({value, min, max, labelId, decreaseLabel, increaseLabel, disabled, onChange }: StepperProps) {
    return(
        <div className={styles.stepper} role="group" aria-labelledby={labelId}>
            <button
            type="button"
            className={styles.button}
            aria-label={decreaseLabel}
            disabled={disabled || value <= min}
            onClick={() => onChange(value - 1)}
            >
                <MinusIcon />
            </button>

            <output className={styles.value} aria-live="polite">
                {value}
            </output>

            <button
            type="button"
            className={styles.button}
            aria-label={increaseLabel}
            disabled={disabled || value >= max}
            onClick={() => onChange(value + 1)}
            >
                <PlusIcon />
            </button>
        </div>
    )
}