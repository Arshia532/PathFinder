import styles from './Header.module.css';
import {Logo} from '../Logo/Logo'
import { FileActions } from './FileActions';

type HeaderProps = {
    onSave: () => void
    onLoadFile: (file: File) => void
}

export function Header ({ onSave, onLoadFile }: HeaderProps) {
    return (
        <header className={styles.header}>
            <div className={styles.brand}>
                <Logo />
                <h1 className={styles.title}>
                    <span className={styles.titleLine}>PathFinder</span>{" "}
                    <span className={styles.titleLine}>Visualizer</span>
                </h1>
            </div>
            <FileActions onSave={onSave} onLoadFile={onLoadFile} />
        </header>
    );
}