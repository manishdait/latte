import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ResetPasswordRequest, UserResponse } from '../../../model/user.type';
import { UserService } from '../../../service/user.service';
import { AlertService } from '../../../service/alert.service';
import { PasswordComponent } from '../../password/password.component';

@Component({
  selector: 'app-password-form',
  imports: [ReactiveFormsModule, PasswordComponent],
  templateUrl: './password-form.component.html',
  styleUrl: './password-form.component.css'
})
export class PasswordFormComponent implements OnInit {
  @Input('user') user: UserResponse | undefined;
  @Output('cancel') cancel: EventEmitter<boolean> = new EventEmitter();

  form: FormGroup;
  formErrors: boolean = false;

  constructor(private userService: UserService, private alertService: AlertService) {
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
      this.formErrors = true;
      return;
    }

    this.formErrors = false;
    const request: ResetPasswordRequest = {
      updatePassword: this.form.get('updatedPassword')?.value,
      confirmPassword: this.form.get('confirmPassword')?.value
    }

    if(this.user) {
      this.userService.resetPasswordForUser(request, this.user.email).subscribe({
        next: () => {
          this.alertService.alert = 'Updated user information';
          this.cancel.emit(true);
        },
        error: (err) => {
          this.alertService.alert = err.error.error;
          this.form.reset();
        }
      });
    }
  }
}
