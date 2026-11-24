import { Icon, type IconBaseProps } from './Icon'

export function ShuffleIcon(props: IconBaseProps) {
  return (
    <Icon {...props}>
      <path d="M16 3h5v5" />
      <path d="M4 20 21 3" />
      <path d="M21 16v5h-5" />
      <path d="m15 15 6 6" />
      <path d="M4 4l5 5" />
    </Icon>
  )
}