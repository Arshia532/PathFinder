import type { ReactNode } from "react";
import styles from './Field.module.css'

type FieldProps = {
    label: string 
    htmlFor?: string
    labelId?: string
    trailing?: ReactNode
    children: ReactNode
}

export function Field({label, htmlFor, labelId, trailing, children}: FieldProps) {
    return (
        <div>
            <div className={styles.header}>
                {htmlFor ? (
                    <label htmlFor={htmlFor} className={styles.label}>
                        {label}
                    </label>
                ) : (
                    <span id={labelId} className={styles.label}>
                        {label}
                    </span>
                )}
                {trailing}
            </div>
            {children}
        </div>
    );
}