import { Component, inject, OnInit, signal } from '@angular/core';
import { UserService } from '../../service/user.service';
import { UserResponse } from '../../model/user.type';
import { EditUserComponent } from '../../components/form/edit-user-form/edit-user.component';
import { Store } from '@ngrx/store';
import { AppState } from '../../state/app.state';
import { Observable } from 'rxjs';
import { users, userCount } from '../../state/user/user.selector';
import { removeUser, setUserCount, setUsers, updateUserCount } from '../../state/user/user.action';
import { CommonModule } from '@angular/common';
import { AlertService } from '../../service/alert.service';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { UserFormComponent } from '../../components/form/user-from/user-form.component';
import { PasswordFormComponent } from '../../components/form/password-form/password-form.component';
import { DialogComponent } from '../../components/dialog/dialog.component';
import { PaginationComponent } from '../../components/pagination/pagination.component';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-user',
  imports: [PaginationComponent, UserFormComponent, EditUserComponent, PasswordFormComponent, CommonModule, DialogComponent, FontAwesomeModule],
  templateUrl: './user.component.html',
  styleUrl: './user.component.css'
})
export class UserComponent implements OnInit {
  userService = inject(UserService);
  alertService = inject(AlertService);
  faLibrary = inject(FaIconLibrary);
  authService = inject(AuthService);
  
  buffer = signal<UserResponse>({
    firstname: '',
    email: '',
    role: {
      id: 0,
      role: '',
      authorities: [],
      editable: false,
      deletable: false
    },
    editable: false,
    deletable: false
  });
  
  editUser = signal(false);
  createUser = signal(false);
  resetPassword = signal(false);
  confirm = signal(false);
  
  page = signal(0);
  size = signal(10);
  userPage = signal<Record<string, boolean>> ({
    'prev': false,
    'next': false
  });
  
  users$: Observable<UserResponse[]>;
  userCount$: Observable<number>;

  loading = signal(true);
  
  constructor(private store: Store<AppState>) {
    this.users$ = store.select(users);
    this.userCount$ = store.select(userCount);
  }

  ngOnInit(): void {
    this.userService.fetchUserCount().subscribe({
      next: (res) => {
        this.store.dispatch(setUserCount({count: res['user_count']}))
      }
    })

    this.getUsers();
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  toggleCreateUser() {
    this.createUser.update(toggle => !toggle);
  }

  toggleEditUser(user: UserResponse) {
    this.buffer.set(user);
    this.editUser.set(true);
  }

  toggleResetPassword(user: UserResponse) {
    this.buffer.set(user);
    this.resetPassword.set(true);
  }

  toggleConfirm() {
    this.confirm.update(toggle => !toggle);
  } 

  createUserOps() {
    return this.authService.createUser();
  }

  editUserOps() {
    return this.authService.editUser();
  }

  deleteUserOps() {
    return this.authService.deleteUser();
  }

  resetPasswordOps() {
    return this.authService.resetUserPassword();
  }

  deleteUser(user: UserResponse) {
    this.buffer.set(user);
    this.toggleConfirm();
  }

  confirmTrigger(event: boolean) {
    this.toggleConfirm();

    if (event) {
      this.userService.deleteUser(this.buffer().email).subscribe({
        next: (response) => {
          this.store.dispatch(removeUser({email: this.buffer().email}));
          this.store.dispatch(updateUserCount({count: -1}));
          this.alertService.alert = `User with name ${this.buffer().firstname} deleted`;
        }
      })
    }
  }

  getNext() {
    this.page.update(count => count + 1);
    this.getUsers();
  }

  getPrev() {
    this.page.update(count => count - 1);
    this.getUsers();
  }

  getUsers() {
    this.loading.set(true);

    this.userService.fetchPagedUsers(this.page(), this.size()).subscribe({
      next: (response) => {
        this.userPage()['prev'] = response.prev;
        this.userPage()['next'] = response.next;
        this.store.dispatch(setUsers({users: response.content}));
        this.loading.set(false);
      }
    });
  }
}
