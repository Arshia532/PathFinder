import { Button } from "../ui/Button";
import { UploadIcon } from "../icons/UploadIcon";
import { DownloadIcon } from "../icons/DownloadIcon";
import styles from './FileActions.module.css'

export function FileActions(){
    return (
        <div className={styles.actions}>
            <input
                id="load-map"
                className={`visually-hidden ${styles.fileInput}`}
                type="file"
                accept=".json,application/json"
            />
            <label htmlFor="load-map" className={styles.loadButton}>
                <UploadIcon size={18} strokeWidth={1.8}/>
                Load map
            </label>
            
            <Button variant="light">
                <DownloadIcon size={18} strokeWidth={1.8}/>
                Save map
            </Button>
        </div>
    )
}