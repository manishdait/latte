import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { Router } from '@angular/router';
import { UserResponse, ResetPasswordRequest } from '../../model/user.type';
import { AlertService } from '../../service/alert.service';
import { AuthService } from '../../service/auth.service';
import { UserService } from '../../service/user.service';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { DialogComponent } from '../../components/dialog/dialog.component';
import { PasswordComponent } from '../../components/password/password.component';
import { Alert } from '../../model/alert.type';

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, FontAwesomeModule, DialogComponent, PasswordComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  authService = inject(AuthService);
  userService = inject(UserService);
  alertService = inject(AlertService);
  faLibrary = inject(FaIconLibrary);
  router = inject(Router);

  userDetails = signal<UserResponse>(this.authService.user);

  buffer = signal<UserResponse>({
    firstname: '',
    email: '',
    role: {
      id: 0,
      role: '',
      authorities: [],
      editable: false,
      deletable: false
    },
    editable: false,
    deletable: false
  });
  
  bufferUpdated = signal(false);
  userDetailsFormError = signal(false);
  
  passwordReset = signal(false);
  passwordResetFormError = signal(false);
  
  confirm = signal(false);
  message = signal('');
  operation = signal<Operation>(Operation.UPDATE_USER);
  
  userDetailsForm: FormGroup;
  passwordResetForm: FormGroup;
  
  constructor() {
    this.userDetailsForm = new FormGroup({
      firstname: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email])
    });

    this.passwordResetForm = new FormGroup({
      updatedPassword: new FormControl('', [Validators.required, Validators.minLength(8)]),
      confirmPassword: new FormControl('', [Validators.required, Validators.minLength(8)])
    })
  } 

  ngOnInit(): void {
    this.userService.fetchUserInfo().subscribe({
      next: (response) => {
        this.userDetails.set(response);
        this.buffer.set({...response});

        if (!this.userDetails().editable) {
          this.userDetailsForm.controls['firstname'].disable({onlySelf: true});
          this.userDetailsForm.controls['email'].disable({onlySelf: true});
        }
        this.userDetailsForm.controls['firstname'].setValue(this.userDetails().firstname);
        this.userDetailsForm.controls['email'].setValue(this.userDetails().email);
      },
      error: (err) => {
        console.error(err);
      }
    });

    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  get userDetailsFormControl() {
    return this.userDetailsForm.controls;
  }

  get passwordResetControl() {
    return this.passwordResetForm.controls;
  }

  changeFirstname(value: string) {
    this.buffer.update(user => {
      return {
        ...user,
        firstname: value
      }
    });

    this.callUpdate();
  }

  changeEmail(value: string) {
    this.buffer.update(user => {
      return {
        ...user,
        email: value
      }
    });
    this.callUpdate();
  }

  onUpdateUserDetails() {
    this.message.set(`You will be logout once your profile is updated`);
    this.operation.set(Operation.UPDATE_USER);
    this.toggleConfirm();
  }

  onResetPassword() {
    this.message.set(`Do you want to reset your password?`);
    this.operation.set(Operation.RESET_PASSWORD);
    this.toggleConfirm();
  }

  toggleConfirm() {
    this.confirm.update(toggle => !toggle);
  }

  confirmTrigger(event: boolean) {
    if (event) {
      if (this.operation() === Operation.UPDATE_USER) {
        this.updateUser();
      } else if (this.operation() === Operation.RESET_PASSWORD) {
        this.resetPassword()
      }
    }
    this.confirm.set(false);
  }

  togglePasswordReset() {
    this.passwordReset.update(toggle => !toggle);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['sign-in'], {replaceUrl: true})
  }

  private callUpdate() {
    if (this.userDetails().firstname === this.buffer().firstname && this.userDetails().email === this.buffer().email) {
        this.bufferUpdated.set(false);
      } else {
      this.bufferUpdated.set(true);
    }
  }

  updateUser() {
    if(this.userDetailsForm.invalid) {
      this.userDetailsFormError.set(true);
      return;
    }
    
    this.userDetailsFormError.set(false);

    const request: UserResponse = {
      firstname: this.userDetailsForm.get('firstname')?.value,
      email: this.userDetailsForm.get('email')?.value,
      role: this.userDetails().role,
      editable: false,
      deletable: false
    }
    
    this.userService.updateUser(request).subscribe({
      next: () => {
        this.router.navigate(['sign-in'], {replaceUrl: true});
      },
      error: (err) => {
        this.passwordResetForm.reset();
        const alert: Alert = {
          title: 'Update Profile',
          message: 'Fail to update your profile',
          type: 'FAIL'
        }
        this.alertService.alert = alert;
      }
    });
  }

  resetPassword() {
    if(this.passwordResetForm.invalid  || 
      this.passwordResetForm.get('updatedPassword')?.value != this.passwordResetForm.get('confirmPassword')?.value) {
      this.passwordResetFormError.set(true);
      return;
    }
    
    this.passwordResetFormError.set(true);
    const request: ResetPasswordRequest = {
      updatePassword: this.passwordResetForm.get('updatedPassword')?.value,
      confirmPassword: this.passwordResetForm.get('confirmPassword')?.value
    }

    this.userService.resetPassword(request).subscribe({
      next: () => {
        this.passwordReset.set(false);
        const alert: Alert = {
          title: 'Password Reset',
          message: 'Your password has been reset',
          type: 'INFO'
        }
        this.alertService.alert = alert;
      },
      error: (err) => {
        this.passwordResetForm.reset();
        const alert: Alert = {
          title: 'Password Reset',
          message: '',
          type: 'WARN'
        }

        if (err.error.status === 400) {
          alert.message = err.error.error;
        } else {
          alert.message = 'Fail to reset your password';
        }
        
        this.alertService.alert = alert;
      }
    })
  }
}

enum Operation {
  UPDATE_USER,
  RESET_PASSWORD
}
