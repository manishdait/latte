import { Component, EventEmitter, input, Input, output, Output } from '@angular/core';

@Component({
  selector: 'app-dialog',
  imports: [],
  templateUrl: './dialog.component.html',
  styleUrl: './dialog.component.css'
})
export class DialogComponent {
  message = input('')
  trigger = output<boolean>();

  toggleTrigger(state: boolean) {
    this.trigger.emit(state);
  }
}
