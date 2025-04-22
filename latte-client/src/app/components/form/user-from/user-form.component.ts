import { Component, EventEmitter, inject, OnInit, output, Output, signal } from '@angular/core';
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

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule, PasswordComponent, DropdownComponent],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.css'
})
export class UserFormComponent implements OnInit {
  authService = inject(AuthService);
  roleService = inject(RoleService);

  cancel = output<boolean>();

  formErrors = signal(false);
  roles = signal<string[]>([]);
  
  form: FormGroup;

  page = signal(0);
  size = signal(5);
  hasNext = signal(false);

  constructor(private alertService: AlertService, private store: Store<AppState>) {
    this.form = new FormGroup({
      firstname: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(8)]),
      role: new FormControl('', [Validators.required])
    })
  }

  ngOnInit(): void {
    this.roleService.getRoles(this.page(), this.size()).subscribe({
      next: (res) => {
        this.roles.set(res.content.map(r => r.role));
        this.hasNext.set(res.next);
      }
    })
  }

  get formControls() {
    return this.form.controls;
  }

  getNext() {
    this.page.update(count => count+1);
    this.roleService.getRoles(this.page(), this.size()).subscribe({
      next: (res) => {
        this.roles.set(res.content.map(r => r.role));
        this.hasNext.set(res.next);
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
