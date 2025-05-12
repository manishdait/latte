import { Component, EventEmitter, inject, input, Input, OnInit, output, Output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ResetPasswordRequest, UserResponse } from '../../../model/user.type';
import { UserService } from '../../../service/user.service';
import { AlertService } from '../../../service/alert.service';
import { PasswordComponent } from '../../password/password.component';
import { Alert } from '../../../model/alert.type';

@Component({
  selector: 'app-password-form',
  imports: [ReactiveFormsModule, PasswordComponent],
  templateUrl: './password-form.component.html',
  styleUrl: './password-form.component.css'
})
export class PasswordFormComponent implements OnInit {
  userService = inject(UserService);
  alertService = inject(AlertService);

  user = input.required<UserResponse>();
  cancel = output<boolean>();
  
  formErrors = signal(false);
  
  form: FormGroup;

  constructor() {
    this.form = new FormGroup({
      updatedPassword: new FormControl('', [Validators.required, Validators.minLength(8)]),
      confirmPassword: new FormControl('', [Validators.required, Validators.minLength(8)])
    });
  }

  ngOnInit(): void {}

  get formControls() {
    return this.form.controls;
  }

  onSubmit() {
    if (this.form.invalid || this.form.get('updatedPassword')?.value != this.form.get('confirmPassword')?.value) {
      this.formErrors.set(true);
      return;
    }

    this.formErrors.set(false);
    const request: ResetPasswordRequest = {
      updatePassword: this.form.get('updatedPassword')?.value,
      confirmPassword: this.form.get('confirmPassword')?.value
    }

    this.userService.resetPasswordForUser(request, this.user().email).subscribe({
      next: () => {
        const alert: Alert = {
          title: 'User Update',
          message: 'Updated user password',
          type: 'INFO'
        }
        this.alertService.alert = alert;
        this.cancel.emit(true);
      },
      error: (err) => {
        const alert: Alert = {
          title: 'User Update',
          message: 'Fail to updated user password',
          type: 'FAIL'
        }
        this.alertService.alert = err.error.error;
        this.form.reset();
      }
    });
  }
}
