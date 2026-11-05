import type { ReactNode } from "react";
import styles from './Field.module.css'

type FieldProps = {
    label: string 
    htmlFor?: string
    labelId?: string
    children: ReactNode
}

export function Field({label, htmlFor, labelId, children}: FieldProps) {
    return (
        <div>
            {htmlFor ? (
                <label htmlFor={htmlFor} className={styles.label}>
                    {label}
                </label>
            ) : (
                <span id={labelId} className={styles.label}>
                    {label}
                </span>
            )}
            {children}
        </div>
    );
}