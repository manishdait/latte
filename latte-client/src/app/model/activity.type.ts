export interface ActivityResponse {
  readonly id: number,
  type: ActivityType,
  author: string,
  message: string,
  createdAt: Date,
  lastUpdated: Date
}

export type ActivityType = 'EDIT' |'COMMENT';
