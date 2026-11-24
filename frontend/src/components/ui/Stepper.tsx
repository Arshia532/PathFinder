import { MinusIcon } from "../icons/MinusIcon";
import { PlusIcon } from "../icons/PlusIcon";
import styles from './Stepper.module.css'

type StepperProps = {
    value: number
    min: number
    max: number
    labelId: string
    decreaseLabel: string
    increaseLabel: string
    onChange: (value: number) => void
}

export function Stepper({value, min, max, labelId, decreaseLabel, increaseLabel, onChange }: StepperProps){ 
    return(
        <div className={styles.stepper} role="group" aria-labelledby={labelId}>
            <button
            type="button"
            className={styles.button}
            aria-label={decreaseLabel}
            disabled={value <= min}
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
            disabled={value >= max}
            onClick={() => onChange(value + 1)}
            >
                <PlusIcon />
            </button>
        </div>
    )
}