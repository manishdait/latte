import { Component, ElementRef, inject, input, Input, OnInit, signal, ViewChild } from '@angular/core';
import { TicketResponse } from '../../model/ticket.type';
import { ActivityResponse, ActivityType } from '../../model/activity.type';
import { CommentRequest } from '../../model/comment.type';
import { ActivityService } from '../../service/activity.service';
import { CommentService } from '../../service/comment.service';
import { getDate } from '../../shared/utils';

@Component({
  selector: 'app-activity',
  imports: [],
  templateUrl: './activity.component.html',
  styleUrl: './activity.component.css'
})
export class ActivityComponent implements OnInit {
  activityService = inject(ActivityService);
  commentService = inject(CommentService);

  ticketId = input.required<number>();

  more = signal(false);
  count = signal(0);
  size = signal(6);

  activities = signal<ActivityResponse[]>([]);

  ngOnInit(): void {
    this.activityService.getActivitiesForTicket(this.ticketId(), this.count(), this.size()).subscribe({
      next: (response) => {
        this.activities.set(response.content);
        this.more.set(response.next);
      }
    });
  }

  getType(type: ActivityType) {
    return type.toString()
  }

  getDate(date: any) {
    return getDate(date);
  }


  loadPrevious() {
    this.count.update(count => count + 1);
    
    this.activityService.getActivitiesForTicket(this.ticketId(), this.count(), this.size()).subscribe({
      next: (response) => {
        this.activities.update(arr => arr.concat(response.content));
        this.more.set(response.next);
      }
    });
  }
}
