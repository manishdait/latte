import { Component, forwardRef, Input } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { CustomControlDirective } from '../../directives/custom-control.directive';

@Component({
  selector: 'app-password',
  imports: [FontAwesomeModule, ReactiveFormsModule],
  templateUrl: './password.component.html',
  styleUrl: './password.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => PasswordComponent),
      multi: true,
    },
  ],
})
export class PasswordComponent extends CustomControlDirective {
  @Input('id') id: string | undefined;
  @Input('placeholder') placeholder: string | undefined;
  
  type: string = 'password';
  
  show() {
    if (this.type === 'password') {
      this.type = 'text';
    } else {
      this.type = 'password';
    }
  }
}
