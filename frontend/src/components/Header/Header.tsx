import styles from './Header.module.css';
import {Logo} from '../Logo/Logo'
import { FileActions } from './FileActions';

export function Header () {
    return (
        <header className={styles.header}>
            <div className={styles.brand}>
                <Logo />
                <h1 className={styles.title}>
                    <span className={styles.titleLine}>PathFinder</span>{" "}
                    <span className={styles.titleLine}>Visualizer</span>
                </h1>
            </div>
            <FileActions />
        </header>
    );
}