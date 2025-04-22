import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms'
import { AuthRequest } from '../../model/auth.type';
import { AuthService } from '../../service/auth.service';
import { Router } from '@angular/router';
import { AlertService } from '../../service/alert.service';
import { PasswordComponent } from '../../components/password/password.component';
import { SpinnerComponent } from '../../components/spinner/spinner.component';

@Component({
  selector: 'app-auth',
  imports: [ReactiveFormsModule, PasswordComponent, SpinnerComponent],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.css'
})
export class AuthComponent implements OnInit {
  alertService = inject(AlertService);
  authService = inject(AuthService);
  router = inject(Router);
  
  formError = signal(false);
  form: FormGroup;
  
  processing = signal(false);
  
  constructor() {
    this.form = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(8)])
    });  
  }

  ngOnInit(): void {}

  get formControls() {
    return this.form.controls;
  }

  onSubmit() {
    if (this.form.invalid) {
      this.formError.set(true);
      return;
    }

    this.formError.set(false);
    var request: AuthRequest = {
      email: this.form.get('email')?.value,
      password: this.form.get('password')?.value
    }
    this.processing.set(true);

    this.authService.authenticateUser(request).subscribe({
      next: () => {
        this.router.navigate(['home'], {replaceUrl: true});
      },
      error: (err) => {
        this.processing.set(false);
        this.form.reset();
        this.alertService.alert = err.error.error;
      }
    });
  }
}
