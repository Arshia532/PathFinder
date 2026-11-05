import { Icon, type IconBaseProps } from './Icon'

export function PlusIcon(props: IconBaseProps) {
  return (
    <Icon {...props}>
      <path d="M5 12h14" />
      <path d="M12 5v14" />
    </Icon>
  )
}