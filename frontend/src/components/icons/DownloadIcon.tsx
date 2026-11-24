import { Icon, type IconBaseProps } from './Icon'

export function DownloadIcon(props: IconBaseProps) {
  return (
    <Icon {...props}>
      <path d="M4 20h16" />
      <path d="M12 4v12" />
      <path d="m7 11 5 5 5-5" />
    </Icon>
  )
}