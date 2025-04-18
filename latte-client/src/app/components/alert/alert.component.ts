import { Component, signal } from '@angular/core';
import { AlertService } from '../../service/alert.service';

@Component({
  selector: 'app-alert',
  imports: [],
  templateUrl: './alert.component.html',
  styleUrl: './alert.component.css'
})
export class AlertComponent {
  message =  signal<string>('');

  constructor(private alertService: AlertService) {
    alertService.alert$.subscribe((message) => {
      this.message.set(message ?? '');
      this.reset();
    })
  }

  reset() {
    setTimeout(() => {
      this.message.set('');
    }, 4000)
  }
}
