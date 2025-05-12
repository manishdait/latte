export type AlertType = 'INFO' | 'WARN' | 'FAIL';

export interface Alert {
  title: string,
  message: string,
  type: AlertType
}
