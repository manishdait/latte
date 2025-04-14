import { Component, EventEmitter, OnInit, output, Output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RegistrationRequest } from '../../../model/auth.type';
import { Role, roles } from '../../../model/role.enum';
import { AuthService } from '../../../service/auth.service';
import { Store } from '@ngrx/store';
import { AppState } from '../../../state/app.state';
import { addUser, incrementUserCount } from '../../../state/user/user.action';
import { UserResponse } from '../../../model/user.type';
import { AlertService } from '../../../service/alert.service';
import { PasswordComponent } from '../../password/password.component';
import { DropdownComponent } from '../../dropdown/dropdown.component';

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule, PasswordComponent, DropdownComponent],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.css'
})
export class UserFormComponent implements OnInit {
  cancel = output<boolean>();

  formErrors = signal(false);
  roles = signal<string[]>(roles);
  
  form: FormGroup;

  constructor(private authService: AuthService, private alertService: AlertService, private store: Store<AppState>) {
    this.form = new FormGroup({
      firstname: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(8)]),
      role: new FormControl('User', [Validators.required])
    })
  }

  ngOnInit(): void {}

  get formControls() {
    return this.form.controls;
  }

  onSubmit() {
    if (this.form.invalid) {
      this.formErrors.set(true);
      return;
    }

    this.formErrors.set(false);
    
    const request: RegistrationRequest = {
      firstname: this.form.get('firstname')?.value,
      email: this.form.get('email')?.value,
      password: this.form.get('password')?.value,
      role: this.form.get('role')?.value === 'Admin'? Role.ADMIN : Role.USER
    }
    
    this.authService.registerUser(request).subscribe({
      next: (response) => {
        const user: UserResponse = {
          firstname: request.firstname,
          email: request.email,
          role: request.role,
          editable: false,
          deletable: false
        }
        this.store.dispatch(addUser({user: user}));
        this.store.dispatch(incrementUserCount());
        this.alertService.alert = `User created with name ${user.firstname}`;
        this.cancel.emit(true);
      },
      error: (err) => {
        this.form.reset();
        this.form.controls['role'].setValue('User');
        this.alertService.alert = err.error.error
      }
    })
  }
}
