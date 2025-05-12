import { Component, signal } from '@angular/core';
import { AlertService } from '../../service/alert.service';
import { Alert, AlertType } from '../../model/alert.type';

@Component({
  selector: 'app-alert',
  imports: [],
  templateUrl: './alert.component.html',
  styleUrl: './alert.component.css'
})
export class AlertComponent {
  alert: Alert | undefined;

  constructor(private alertService: AlertService) {
    alertService.alert$.subscribe((alert) => {
      this.alert = alert;
      this.reset();
    })
  }

  reset() {
    setTimeout(() => {
      this.alert = undefined
    }, 4000);
  }
}
