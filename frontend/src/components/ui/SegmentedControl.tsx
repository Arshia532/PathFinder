import styles from './SegmentedControl.module.css'

export type SegmentOption = {
    value: string 
    label: string
    keyColor?: string
    keyOutline?: boolean
}

type SegmentedControlProps = {
    options: SegmentOption[]
    selected?: string
    labelId: string
}

export function SegmentedControl ({options, selected, labelId}: SegmentedControlProps){
    return(
        <div className={styles.track} role='group' aria-labelledby={labelId}>
            {options.map((option) => {
                const isSelected = option.value === selected;

                return (
                    <button
                    key={option.value}
                    type='button'
                    className={`${styles.segment} ${isSelected ? styles.selected : ''}`}
                    aria-pressed={isSelected}
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