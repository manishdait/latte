import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { RoleService } from '../../../service/role.service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Role, RoleRequest, Authority } from '../../../model/role.type';

@Component({
  selector: 'app-edit-role-form',
  imports: [ReactiveFormsModule],
  templateUrl: './edit-role-form.component.html',
  styleUrl: './edit-role-form.component.css'
})
export class EditRoleFormComponent implements OnInit {
  roleService = inject(RoleService);

  id = input.required<number>();
  cancel = output<boolean>();

  permissions: Authority[] = [
    'user::create', 'user::edit', 'user::delete', 'user::reset-password', 'ticket::create', 'ticket::edit', 
    'ticket::delete', 'ticket::lock-unlock', 'ticket::assign', 'role::create', 'role::edit', 'role::delete', 
    'client::create', 'client::edit', 'client::delete'
  ];
  formError = signal(false);

  bufferRole = signal<Role>({
    id: 0,
    role: '',
    editable: false,
    deletable: false,
    authorities: []
  })
  form: FormGroup;

  constructor() {
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
      'client::delete': new FormControl(false)
    });
  }

  ngOnInit(): void {
    this.roleService.getRole(this.id()).subscribe({
      next: (res) => {
        this.bufferRole.set({...res});
        this.form.controls['role'].setValue(res.role);
        for (let authority of res.authorities) {
          this.form.controls[authority].setValue(true);
        }
      }
    });
  }

  get formControl() {
    return this.form.controls;
  }

  onSubmit() {
    let updatedAuhtorities: Authority[] = [];

    const request: RoleRequest = {
      role: null,
      authorities: null
    }

    if (this.bufferRole().role !== this.form.get('role')?.value) {
      request.role = this.form.get('role')?.value;
    }

    for(let permission of this.permissions) {
      if (this.form.get(permission)?.value) {
        updatedAuhtorities.push(permission);
      }
    }
    if (updatedAuhtorities.length > 0) {
      request.authorities = updatedAuhtorities;
    }
    
    this.roleService.updateRole(this.id(), request).subscribe({
      next: (res) => {
        this.cancel.emit(true);
      }
    })
  }
}
