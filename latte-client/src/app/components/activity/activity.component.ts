import { Component, inject, input, OnInit, signal } from '@angular/core';
import { ActivityResponse, ActivityType } from '../../model/activity.type';
import { CommentDto } from '../../model/comment.type';
import { ActivityService } from '../../service/activity.service';
import { CommentService } from '../../service/comment.service';
import { getDate } from '../../shared/utils';
import { CommentBoxComponent } from '../comment-box/comment-box.component';

@Component({
  selector: 'app-activity',
  imports: [CommentBoxComponent],
  templateUrl: './activity.component.html',
  styleUrl: './activity.component.css'
})
export class ActivityComponent implements OnInit {
  activityService = inject(ActivityService);
  commentService = inject(CommentService);

  ticketId = input.required<number>();

  hasMore = signal(false);
  page = signal(0);
  size = signal(6);

  activities = signal<ActivityResponse[]>([]);

  ngOnInit(): void {
    this.activityService.getActivitiesForTicket(this.ticketId(), this.page(), this.size()).subscribe({
      next: (response) => {
        this.activities.set(response.content);
        this.hasMore.set(response.next);
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
    this.page.update(count => count + 1);
    
    this.activityService.getActivitiesForTicket(this.ticketId(), this.page(), this.size()).subscribe({
      next: (response) => {
        this.activities.update(arr => arr.concat(response.content));
        this.hasMore.set(response.next);
      }
    });
  }

  refresh() {
    this.ngOnInit();
  }
}
