import { Component, inject, OnInit, output, Output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RegistrationRequest } from '../../../model/auth.type';
import { AuthService } from '../../../service/auth.service';
import { Store } from '@ngrx/store';
import { AppState } from '../../../state/app.state';
import { addUser, updateUserCount } from '../../../state/user/user.action';
import { UserResponse } from '../../../model/user.type';
import { AlertService } from '../../../service/alert.service';
import { PasswordComponent } from '../../password/password.component';
import { DropdownComponent } from '../../dropdown/dropdown.component';
import { RoleService } from '../../../service/role.service';
import { Alert } from '../../../model/alert.type';
import { SpinnerComponent } from '../../spinner/spinner.component';

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule, PasswordComponent, DropdownComponent, SpinnerComponent],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.css'
})
export class UserFormComponent implements OnInit {
  authService = inject(AuthService);
  roleService = inject(RoleService);

  cancel = output<boolean>();

  formErrors = signal(false);
  loadingRoles = signal(false);
  roles = signal<string[]>([]);
  
  form: FormGroup;

  page = signal(0);
  size = signal(5);
  hasNext = signal(false);

  processing = signal(false);

  constructor(private alertService: AlertService, private store: Store<AppState>) {
    this.form = new FormGroup({
      firstname: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(8)]),
      role: new FormControl('', [Validators.required])
    })
  }

  ngOnInit(): void {
    this.loadingRoles.set(true);
    this.roleService.getRoles(this.page(), this.size()).subscribe({
      next: (res) => {
        this.roles.set(res.content.map(r => r.role));
        this.hasNext.set(res.next);
        this.loadingRoles.set(false);
      }
    })
  }

  get formControls() {
    return this.form.controls;
  }

  getNext() {
    this.page.update(count => count+1);
    this.loadingRoles.set(true);
    this.roleService.getRoles(this.page(), this.size()).subscribe({
      next: (res) => {
        this.roles.update(roles => roles = [...roles, ...res.content.map(r => r.role)]);
        this.hasNext.set(res.next);
        this.loadingRoles.set(false);
      }
    })
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
      role: this.form.get('role')?.value
    }

    this.processing.set(true);
    this.form.disable();
    
    this.authService.registerUser(request).subscribe({
      next: (res) => {
        const user: UserResponse = {
          firstname: res.firstname,
          email: res.email,
          role: res.role,
          editable: true,
          deletable: true
        }
        this.store.dispatch(addUser({user: user}));
        this.store.dispatch(updateUserCount({count: 1}));
        
        const alert: Alert = {
          title: 'User Created',
          message: `User created with username ${res.firstname}`,
          type: 'INFO'
        }

        this.alertService.alert = alert;
        this.cancel.emit(true);
      },
      error: (err) => {
        this.form.reset();
        this.form.controls['role'].setValue('User');

        this.processing.set(false);
        this.form.enable();
        
        const alert: Alert = {
          title: '',
          message: ``,
          type: 'INFO'
        }

        if (err.error.status === 400) {
          alert.title = 'Create User';
          alert.message = err.error.error;
          alert.type = 'WARN';
        } else {
          alert.title = 'Create User';
          alert.message = 'Fail to create new user';
          alert.type = 'FAIL';
        }

        this.alertService.alert = alert;
      }
    })
  }
}
