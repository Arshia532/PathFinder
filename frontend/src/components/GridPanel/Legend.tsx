import styles from "./Legend.module.css";

const entries = [
  { label: "Start", colour: styles.start },
  { label: "End", colour: styles.end },
  { label: "Wall", colour: styles.wall },
  { label: "Visited", colour: styles.visited },
  { label: "Frontier", colour: styles.frontier },
  { label: "Current", colour: styles.current },
  { label: "Path found", colour: styles.path },
];

export function Legend() {
  return (
    <ul className={styles.legend}>
      {entries.map((entry) => (
        <li key={entry.label} className={styles.item}>
          <span
            className={`${styles.key} ${entry.colour}`}
            aria-hidden="true"
          />
          {entry.label}
        </li>
      ))}
    </ul>
  );
}
