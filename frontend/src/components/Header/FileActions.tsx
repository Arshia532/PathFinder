import { Button } from "../ui/Button";
import { UploadIcon } from "../icons/UploadIcon";
import { DownloadIcon } from "../icons/DownloadIcon";
import styles from './FileActions.module.css'

type FileActionsProps = {
    onSave: () => void
    onLoadFile: (file: File) => void
}

export function FileActions({ onSave, onLoadFile }: FileActionsProps){
    return (
        <div className={styles.actions}>
            <input
                id="load-map"
                className={`visually-hidden ${styles.fileInput}`}
                type="file"
                accept=".json,application/json"
                onChange={(e) => {
                    const file = e.currentTarget.files?.[0]
                    e.currentTarget.value = '' // so picking the same file again still fires
                    if (file) onLoadFile(file)
                }}
            />
            <label htmlFor="load-map" className={styles.loadButton}>
                <UploadIcon size={18} strokeWidth={1.8}/>
                Load map
            </label>
            
            <Button variant="light" onClick={onSave}>
                <DownloadIcon size={18} strokeWidth={1.8}/>
                Save map
            </Button>
        </div>
    )
}