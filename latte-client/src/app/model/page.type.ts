export interface Page <T> {
  content: T[],
  next: boolean,
  previous: boolean,
  totalElement: number
}
