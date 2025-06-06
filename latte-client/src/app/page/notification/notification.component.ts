import { Component, inject, OnInit, signal } from '@angular/core';
import { Store } from '@ngrx/store';
import { AppState } from '../../state/app.state';
import { count, Observable } from 'rxjs';
import { notifications } from '../../state/notification/notification.selector';
import { CommonModule } from '@angular/common';
import { Notification } from '../../model/notification.type';
import { getDate } from '../../shared/utils';
import { NotificationService } from '../../service/notification.service';
import { setNotification, setRecentNotification } from '../../state/notification/notification.action';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { ShimmerComponent } from '../../components/shimmer/shimmer.component';
import { PaginationComponent } from '../../components/pagination/pagination.component';

@Component({
  selector: 'app-notification',
  imports: [CommonModule, FontAwesomeModule, PaginationComponent, ShimmerComponent],
  templateUrl: './notification.component.html',
  styleUrl: './notification.component.css'
})
export class NotificationComponent implements OnInit {
  notificationService = inject(NotificationService);
  faLibrary = inject(FaIconLibrary)

  notifications$: Observable<Notification[]>;

  loading = signal(false);

  page = signal(0);
  size = signal(10);
  notificationPage = signal({
    'hasNext': false,
    'hasPrevious': false
  });

  constructor(private store: Store<AppState>) {
    this.notifications$ = store.select(notifications);
  }

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
    this.getNotifications();
    this.store.dispatch(setRecentNotification({status: false}));
  }

  delete(id: number) {
    this.notificationService.deleteNotification(id).subscribe({
      next: () => {
        this.ngOnInit();
      }
    })
  }

  getNext() {
    this.page.update(count => count + 1);
    this.getNotifications();
  }

  getPrevious() {
    this.page.update(count => count - 1);
    this.getNotifications();
  }

  getNotifications() {
    this.loading.set(true);
    this.notificationService.fetchNotification(this.page(), this.size()).subscribe({
      next: (res) => {
        this.store.dispatch(setNotification({notifications: res.content}));

        this.notificationPage.update(page => {
          page.hasNext = res.next;
          page.hasPrevious = res.previous;
          return page;
        });

        this.loading.set(false);
      }
    });
  }

  getDate(date: any) {
    return getDate(date);
  }
}
