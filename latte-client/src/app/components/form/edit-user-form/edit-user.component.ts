import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { UserResponse } from '../../../model/user.type';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Role, roles } from '../../../model/role.enum';
import { UserService } from '../../../service/user.service';
import { AlertService } from '../../../service/alert.service';
import { DropdownComponent } from '../../dropdown/dropdown.component';

@Component({
  selector: 'app-edit-user',
  imports: [ReactiveFormsModule, DropdownComponent],
  templateUrl: './edit-user.component.html',
  styleUrl: './edit-user.component.css'
})
export class EditUserComponent implements OnInit {
  @Input('user') user: UserResponse | undefined;
  @Output('cancel') cancel: EventEmitter<boolean> = new EventEmitter();

  form: FormGroup;
  formErrors: boolean = false;
  
  _user: string | undefined;
  roles: string[] = roles;
  
  constructor(private userService: UserService, private alertService: AlertService) {
    this.form = new FormGroup({
      firstname: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      role: new FormControl('User', [Validators.required])
    })
  }

  ngOnInit(): void {
    this._user = this.user?.email;

    this.form.controls['firstname'].setValue(this.user?.firstname);
    this.form.controls['email'].setValue(this.user?.email);
    this.form.controls['role'].setValue(this.user?.role === Role.ADMIN? 'Admin' : 'User');
  }
  
  get formControls() {
    return this.form.controls;
  }

  onSubmit() {
    if (this.form.invalid) {
      this.formErrors = true;
      return;
    }
  
    this.formErrors = false;

    const request: UserResponse = {
      firstname: this.form.get('firstname')?.value,
      email: this.form.get('email')?.value,
      role: this.form.get('role')?.value === 'Admin'? Role.ADMIN : Role.USER
    }

    if (this._user) {
      this.userService.editUser(request, this._user).subscribe({
        next: (response) => {
          this.alertService.alert = 'Updated user info';
          this.cancel.emit(true);
        },
        error: (err) => {
          this.form.reset();
          this.form.controls['role'].setValue(this.user?.role == Role.ADMIN? 'Admin' : 'User');
          this.alertService.alert = err.error.error;
        }
      })
    }
  }
}
