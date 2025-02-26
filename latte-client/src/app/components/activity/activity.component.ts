import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { ActivityService } from '../../service/activity.service';
import { ActivityResponse, ActivityType } from '../../models/activity.type';
import { CommentService } from '../../service/comment.service';
import { CommentRequest } from '../../models/comment.type';
import { getDate } from '../../shared/utils';

@Component({
  selector: 'app-activity',
  imports: [],
  templateUrl: './activity.component.html',
  styleUrl: './activity.component.css'
})
export class ActivityComponent implements OnInit {
  @ViewChild('message') message!: ElementRef;
  @Input('ticketId') ticketId: number | undefined;

  hasMore: boolean = false;

  count: number = 0;
  size: number = 6;

  activities: ActivityResponse[] = [];

  constructor(private activityService: ActivityService, private commentService: CommentService) {}

  ngOnInit(): void {
    if(this.ticketId) {
      this.activityService.getActivitiesForTicket(this.ticketId, this.count, this.size).subscribe({
        next: (response) => {
          this.activities = response.content;
          this.hasMore = response.next;
        }
      });
    }
  }

  getType(type: ActivityType) {
    return type.toString()
  }

  getDate(date: any) {
    return getDate(date);
  }

  comment(message: string) {
    const input = this.message.nativeElement as HTMLInputElement;
    if (this.ticketId && message !== '') {
      const request: CommentRequest = {
        ticketId: this.ticketId,
        message: message
      }
      input.value = '';
      this.commentService.createComment(request).subscribe({
        next: () => {
          this.ngOnInit();
        }
      })
    }
  }

  loadPrevious() {
    if (this.ticketId) {
      this.count += 1;
    
      this.activityService.getActivitiesForTicket(this.ticketId, this.count, this.size).subscribe({
        next: (response) => {
          this.activities = this.activities.concat(response.content);
          this.hasMore = response.next;
        }
      });
    }
  }
}
