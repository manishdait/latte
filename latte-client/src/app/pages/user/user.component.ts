import { Component, OnInit } from '@angular/core';
import { UserService } from '../../service/user.service';
import { UserResponse } from '../../models/user.type';
import { EditUserComponent } from '../../forms/edit-user/edit-user.component';
import { CreateUserComponent } from '../../forms/create-user/create-user.component';
import { ResetPasswordComponent } from '../../forms/reset-password/reset-password.component';
import { Store } from '@ngrx/store';
import { AppState } from '../../state/app.state';
import { Observable } from 'rxjs';
import { userSelector } from '../../state/user/user.selector';
import { removeUser, setUsers } from '../../state/user/user.action';
import { CommonModule } from '@angular/common';
import { ConfirmComponent } from '../../forms/confirm/confirm.component';
import { AlertService } from '../../service/alert.service';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';

@Component({
  selector: 'app-user',
  imports: [CreateUserComponent, EditUserComponent, ResetPasswordComponent, CommonModule, ConfirmComponent, FontAwesomeModule],
  templateUrl: './user.component.html',
  styleUrl: './user.component.css'
})
export class UserComponent implements OnInit {
  user: UserResponse | undefined;
  users$: Observable<UserResponse[]>;

  editToggle: boolean = false;
  createToggle: boolean = false;
  resetToggle: boolean = false;
  confirmToggle: boolean = false;

  count: number = 0;
  size: number = 2;
  page: Record<string, boolean> = {
    'prev': false,
    'next': false
  }
  constructor(private userService: UserService, private alertService: AlertService, private faLibrary: FaIconLibrary, private store: Store<AppState>) {
    this.users$ = store.select(userSelector);
  }

  ngOnInit(): void {
    this.userService.fetchPagedUsers(this.count, this.size).subscribe({
      next: (response) => {
        this.page['prev'] = response.prev;
        this.page['next'] = response.next;
        this.store.dispatch(setUsers({users: response.content}));
      }
    });

    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  createUser() {
    this.createToggle = true;
  }

  edit(user: UserResponse) {
    this.user = user;
    this.editToggle = true;
  }

  resetPassword(user: UserResponse) {
    this.user = user;
    this.resetToggle = true;
  }

  delete(user: UserResponse) {
    this.user = user;
    this.confirm();
  }

  confirm() {
    this.confirmToggle = true;
  } 

  trigger(event: boolean) {
    this.confirmToggle = false;
    
    if (event && this.user) {
      this.userService.deleteUser(this.user.email).subscribe({
        next: (response) => {
          this.store.dispatch(removeUser({email: this.user!.email}))
          this.alertService.alert = `User with name ${this.user?.firstname} deleted`;
        }
      })
    }
  }

  next() {
    this.count += 1;
    this.userService.fetchPagedUsers(this.count, this.size).subscribe({
      next: (response) => {
        this.page['prev'] = response.prev;
        this.page['next'] = response.next;
        this.store.dispatch(setUsers({users: response.content}));
      }
    });
  }

  prev() {
    this.count -= 1;
    this.userService.fetchPagedUsers(this.count, this.size).subscribe({
      next: (response) => {
        this.page['prev'] = response.prev;
        this.page['next'] = response.next;
        this.store.dispatch(setUsers({users: response.content}));
      }
    });
  }
}
