import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TicketFormComponent } from '../../components/form/ticket-form/ticket-form.component';
import { MenubarComponent } from '../../components/menubar/menubar.component';
import { NotificationService } from '../../service/notification.service';
import { Store } from '@ngrx/store';
import { AppState } from '../../state/app.state';
import { addNotification } from '../../state/notification/notification.action';
import { AlertService } from '../../service/alert.service';
import { Alert } from '../../model/alert.type';

@Component({
  selector: 'app-home',
  imports: [RouterOutlet, MenubarComponent, TicketFormComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  notificationService = inject(NotificationService);
  alertService = inject(AlertService);

  createTicket = signal(false);

  constructor(private store: Store<AppState>) {
    this.notificationService.message().subscribe({
      next: (message) => {
        store.dispatch(addNotification({notification: message}));
        const alert: Alert = {
          title: 'Notification',
          message: message.message,
          type: 'INFO'
        }
        this.alertService.alert = alert;
      }
    })
  }

  ngOnInit(): void {
    this.notificationService.connect();
  }

  createTicketToggle() {
    this.createTicket.update(toggle => !toggle);
  }
}
