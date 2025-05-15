import { Component, inject, OnInit, signal } from '@angular/core';
import { Store } from '@ngrx/store';
import { AppState } from '../../state/app.state';
import { Observable } from 'rxjs';
import { notifications } from '../../state/notification/notification.selector';
import { CommonModule } from '@angular/common';
import { Notification } from '../../model/notification.type';
import { getDate } from '../../shared/utils';
import { NotificationService } from '../../service/notification.service';
import { setNotification } from '../../state/notification/notification.action';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { ShimmerComponent } from '../../components/shimmer/shimmer.component';

@Component({
  selector: 'app-notification',
  imports: [CommonModule, FontAwesomeModule, ShimmerComponent],
  templateUrl: './notification.component.html',
  styleUrl: './notification.component.css'
})
export class NotificationComponent implements OnInit {
  notificationService = inject(NotificationService);
  faLibrary = inject(FaIconLibrary)

  notifications$: Observable<Notification[]>;

  loading = signal(false);

  constructor(private store: Store<AppState>) {
    this.notifications$ = store.select(notifications);
  }

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
    this.loading.set(true);
    this.notificationService.fetchNotification().subscribe({
      next: (res) => {
        this.store.dispatch(setNotification({notifications: res}));
        this.loading.set(false);
      }
    });
  }

  delete(id: number) {
    this.notificationService.deleteNotification(id).subscribe({
      next: () => {
        this.ngOnInit();
      }
    })
  }

  getDate(date: any) {
    return getDate(date);
  }
}
