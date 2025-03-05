export interface Page <T> {
  content: T[],
  next: boolean,
  prev: boolean
}