import { LockIcon } from "../../icons/LockIcon";
import styles from './ControlsTabs.module.css';

type TabOption = {
    id: string
    label: string
    dotColor: string
    locked?: boolean
}

const tabOptions: TabOption[] = [
    { id: 'map', label: 'Map', dotColor: 'var(--color-dot-map)' },
    { id: 'terrain', label: 'Terrain', dotColor: 'var(--color-dot-terrain)', locked: true },
]

type ControlsTabsProps = {
    selected: string
    onSelect: (id: string) => void;
}

export function ControlsTabs ({ selected, onSelect }: ControlsTabsProps) {
    return(
        <div className={styles.tabs} role='tablist' aria-label="Map Settings">
            {tabOptions.map((tab) => {
                const isSelected = tab.id === selected
                return (
                    <button
                    key={tab.id}
                    type="button"
                    id={`tab-${tab.id}`}
                    className={`${styles.tab} ${isSelected ? styles.selected : ''}`}
                    role="tab"
                    aria-selected={isSelected}
                    aria-controls={`panel-${tab.id}`}
                    onClick={() => onSelect(tab.id)}
                    >
                        <span className={styles.dot} style={{ background: tab.dotColor }}  aria-hidden='true' />
                        {tab.label}
                        {tab.locked && (
                            <>
                                <LockIcon size={13} strokeWidth={2.2}/>
                                <span className="visually-hidden">(locked)</span>
                            </>
                        )}
                    </button>
                )
            })}
        </div>
    )
}