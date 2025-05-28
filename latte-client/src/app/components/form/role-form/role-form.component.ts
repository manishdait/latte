import { Component, inject, output, signal } from '@angular/core';
import { RoleService } from '../../../service/role.service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RoleRequest, Authority } from '../../../model/role.type';
import { Store } from '@ngrx/store';
import { AppState } from '../../../state/app.state';
import { addRole, updateRoleCount } from '../../../state/role/role.action';
import { SpinnerComponent } from '../../spinner/spinner.component';

@Component({
  selector: 'app-role-form',
  imports: [ReactiveFormsModule, SpinnerComponent],
  templateUrl: './role-form.component.html',
  styleUrl: './role-form.component.css'
})
export class RoleFormComponent {
  roleService = inject(RoleService);

  cancel = output<boolean>();

  permissions: Authority[] = [
    'user::create', 'user::delete',  'user::delete', 'user::reset-password', 'ticket::create', 'ticket::edit', 
    'ticket::delete', 'ticket::lock-unlock', 'ticket::assign', 'role::create', 'role::edit', 'role::delete', 
    'client::create', 'client::edit', 'client::delete'
  ];
  formError = signal(false);
  form: FormGroup;

  processing = signal(false);
  constructor(private store: Store<AppState>) {
    this.form = new FormGroup({
      'role': new FormControl('', [Validators.required]),
      'user::create': new FormControl(false),
      'user::edit': new FormControl(false),
      'user::delete': new FormControl(false),
      'user::reset-password': new FormControl(false),
      'ticket::create': new FormControl(false),
      'ticket::edit': new FormControl(false),
      'ticket::assign': new FormControl(false),
      'ticket::lock-unlock': new FormControl(false),
      'ticket::delete': new FormControl(false),
      'role::create': new FormControl(false),
      'role::edit': new FormControl(false),
      'role::delete': new FormControl(false),
      'client::create': new FormControl(false),
      'client::edit': new FormControl(false),
      'client::delete': new FormControl(false),
    });
  }

  get formControl() {
    return this.form.controls;
  }

  onSubmit() {
    if (this.form.invalid) {
      this.formError.set(true);
      return;
    }

    this.formError.set(false);

    const request: RoleRequest = {
      role: this.form.get('role')?.value,
      authorities: []
    }

    for (let permission of this.permissions) {
      if (this.form.get(permission)?.value === true) {
        request.authorities?.push(permission);
      }
    }

    this.processing.set(true);
    this.form.disable();

    this.roleService.createRole(request).subscribe({
      next: (res) => {
        this.store.dispatch(addRole({role: res}));
        this.store.dispatch(updateRoleCount({count: 1}));
        this.cancel.emit(true);
      }, error: (err) => {
        this.processing.set(false);
        this.form.enable();
      }
    })
  }
}
