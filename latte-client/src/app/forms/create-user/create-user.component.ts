import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RegistrationRequest } from '../../models/auth.type';
import { Role } from '../../models/role.enum';
import { AuthService } from '../../service/auth.service';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { Store } from '@ngrx/store';
import { AppState } from '../../state/app.state';
import { addUser, incrementUserCount } from '../../state/user/user.action';
import { UserResponse } from '../../models/user.type';
import { AlertService } from '../../service/alert.service';

@Component({
  selector: 'app-create-user',
  imports: [ReactiveFormsModule, FontAwesomeModule],
  templateUrl: './create-user.component.html',
  styleUrl: './create-user.component.css'
})
export class CreateUserComponent implements OnInit {
  @Output('toggle') toggle: EventEmitter<boolean> = new EventEmitter();

  form: FormGroup;
  formErrors: boolean = false;

  passtype: string = 'password';
  passicon: string = 'eye'

  constructor(private authService: AuthService, private alertService: AlertService, private faLibrary: FaIconLibrary, private store: Store<AppState>) {
    this.form = new FormGroup({
      firstname: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(8)]),
      role: new FormControl('user', [Validators.required])
    })
  }

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
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
    const request: RegistrationRequest = {
      firstname: this.form.get('firstname')?.value,
      email: this.form.get('email')?.value,
      password: this.form.get('password')?.value,
      role: this.form.get('role')?.value === 'admin'? Role.ADMIN : Role.USER
    }
    this.form.reset();
    this.form.controls['role'].setValue('user');
    
    this.authService.registerUser(request).subscribe({
      next: (response) => {
        const user: UserResponse = {
          firstname: request.firstname,
          email: request.email,
          role: request.role
        }
        this.store.dispatch(addUser({user: user}));
        this.store.dispatch(incrementUserCount());
        this.alertService.alert = `User created with name ${user.firstname}`;
        this.cancel();
      },
      error: (err) => {
        this.alertService.alert = err.error.error
      }
    })
  }

  showPassword() {
    if (this.passtype === 'password') {
      this.passtype = 'text';
      this.passicon = 'eye-slash';
    } else {
      this.passtype = 'password';
      this.passicon = 'eye';
    }
  }

  cancel() {
    this.toggle.emit(false);
  }
}
