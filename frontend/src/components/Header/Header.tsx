import styles from './Header.module.css';
import {Logo} from '../Logo/Logo'

export function Header () {
    return (
        <header className={styles.header}>
        <Logo />

            <h1 className={styles.title}>
                <span className={styles.titleLine}>PathFinder</span>{" "}
                <span className={styles.titleLine}>Visualizer</span>
            </h1>
        </header>
    );
}