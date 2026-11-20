import { Icon, type IconBaseProps } from './Icon'

export function PauseIcon(props: IconBaseProps) {
  return (
    <Icon {...props} filled>
      <rect x="6" y="4.5" width="4.2" height="15" rx="1.2" />
      <rect x="13.8" y="4.5" width="4.2" height="15" rx="1.2" />
    </Icon>
  )
}