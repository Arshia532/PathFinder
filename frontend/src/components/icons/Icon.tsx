import type { ReactNode } from "react";

export type IconBaseProps = {
  size?: number;
  strokeWidth?: number;
  className?: string;
};

type IconProps = IconBaseProps & {
  children: ReactNode;
};

export function Icon({
  size = 16,
  strokeWidth = 2,
  className,
  children,
}: IconProps) {
  return (
    <svg
      className={className}
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={strokeWidth}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {children}
    </svg>
  );
}
