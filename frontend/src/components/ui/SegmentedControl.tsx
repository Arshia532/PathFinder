import styles from './SegmentedControl.module.css'

export type SegmentOption<T extends string = string> = {
    value: T
    label: string
    keyColor?: string
    keyOutline?: boolean
}

type SegmentedControlProps<T extends string = string> = {
    options: SegmentOption<T>[]
    selected?: T
    onSelect?: (value: T) => void
    labelId: string
    tone?: 'neutral' | 'accent'
}

export function SegmentedControl<T extends string>({
    options, selected, onSelect, labelId, tone,
    }: SegmentedControlProps<T>) {
    const selectedClass = tone === 'accent' ? styles.selectedAccent : styles.selected
    return(
        <div className={styles.track} role='group' aria-labelledby={labelId}>
            {options.map((option) => {
                const isSelected = option.value === selected;
                return (
                    <button
                    key={option.value}
                    type='button'
                    className={`${styles.segment} ${isSelected ? selectedClass : ''}`}
                    aria-pressed={isSelected}
                    onClick={() => onSelect?.(option.value)}
                    >
                        {option.keyColor && (
                            <span
                            className={`${styles.key} ${option.keyOutline ? styles.keyOutline : ''}`}
                            style={{background: option.keyColor}}
                            aria-hidden="true"
                            />
                        )}
                        {option.label}
                    </button>
                )
            })}
        </div>
    )
}