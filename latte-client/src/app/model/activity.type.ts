export interface ActivityResponse {
  id: number,
  type: ActivityType,
  author: string,
  message: string,
  createdAt: Date,
  lastUpdated: Date
}

export enum ActivityType {
  EDIT,
  COMMENT
}