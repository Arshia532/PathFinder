import { Icon, type IconBaseProps } from './Icon'

export function UploadIcon(props: IconBaseProps) {
  return (
    <Icon {...props}>
      <path d="M4 20h16" />
      <path d="M12 16V4" />
      <path d="m7 9 5-5 5 5" />
    </Icon>
  )
}