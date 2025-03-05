import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-dialog',
  imports: [],
  templateUrl: './dialog.component.html',
  styleUrl: './dialog.component.css'
})
export class DialogComponent {
  @Input('message') message: string | undefined;
  @Output('state') state: EventEmitter<boolean> = new EventEmitter();

  trigger(state: boolean) {
    this.state.emit(state);
  }
}
