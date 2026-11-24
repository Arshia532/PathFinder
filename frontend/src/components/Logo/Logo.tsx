import styles from './Logo.module.css'

type LogoProps = {
  size?: number
}

export function Logo({ size = 45 }: LogoProps) {
  return (
    <svg
      className={styles.logo}
      width={size}
      height={size}
      viewBox="0 0 40 40"
      aria-hidden="true"
    >
      <rect className={styles.tileStart} x="1" y="1" width="18" height="18" rx="5" />
      <rect className={styles.tileCurrent} x="21" y="1" width="18" height="18" rx="5" />
      <rect className={styles.tileEnd} x="1" y="21" width="18" height="18" rx="5" />
      <rect className={styles.tilePath} x="21" y="21" width="18" height="18" rx="5" />
    </svg>
  )
}