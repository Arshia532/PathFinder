import { Icon, type IconBaseProps } from './Icon'

export function RotateCcwIcon(props: IconBaseProps) {
  return (
    <Icon {...props}>
      <path d="M3 12a9 9 0 1 0 2.64-6.36L3 8" />
      <path d="M3 3v5h5" />
    </Icon>
  )
}