import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { UserResponse } from '../../../model/user.type';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../../service/user.service';
import { AlertService } from '../../../service/alert.service';
import { DropdownComponent } from '../../dropdown/dropdown.component';
import { RoleService } from '../../../service/role.service';
import { Alert } from '../../../model/alert.type';
import { SpinnerComponent } from '../../spinner/spinner.component';

@Component({
  selector: 'app-edit-user',
  imports: [ReactiveFormsModule, DropdownComponent, SpinnerComponent],
  templateUrl: './edit-user.component.html',
  styleUrl: './edit-user.component.css'
})
export class EditUserComponent implements OnInit {
  userService = inject(UserService);
  roleService = inject(RoleService);
  alertService = inject(AlertService);

  user = input.required<UserResponse>();
  cancel = output<boolean>();

  formErrors = signal(false);
  
  _user = signal('');
  roles = signal<string[]>([]);
  loadingRoles = signal(false);
  
  form: FormGroup;

  page = signal(0);
  size = signal(5);
  hasNext = signal(false);

  processing = signal(false);
  
  constructor() {
    this.form = new FormGroup({
      firstname: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      role: new FormControl('', [Validators.required])
    })
  }

  ngOnInit(): void {
    this._user.set(this.user().email);

    this.form.controls['firstname'].setValue(this.user().firstname);
    this.form.controls['email'].setValue(this.user().email);
    this.form.controls['role'].setValue(this.user().role.role);

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
        this.roles.set(res.content.map(r => r.role));
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

    const request: UserResponse = {
      firstname: this.form.get('firstname')?.value,
      email: this.form.get('email')?.value,
      role: this.form.get('role')?.value,
      editable: false,
      deletable: false
    }

    if (this._user) {
      this.processing.set(true);
      this.form.disable();

      this.userService.editUser(request, this._user()).subscribe({
        next: () => {
          const alert: Alert = {
          title: 'User Update',
          message: 'Updated user information',
          type: 'INFO'
        }
        this.alertService.alert = alert;
        
        this.cancel.emit(true);
          this.cancel.emit(true);
        },
        error: (err) => {
          this.form.reset();
          this.processing.set(false);
          this.form.enable();
          this.form.controls['role'].setValue(this.user().role.role);
          const alert: Alert = {
            title: 'User Update',
            message: 'Fail updated user information',
            type: 'FAIL'
          }
          this.alertService.alert = alert;
        }
      })
    }
  }
}
