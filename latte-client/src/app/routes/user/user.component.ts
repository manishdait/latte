import { Component, inject, OnInit, signal } from '@angular/core';
import { UserService } from '../../service/user.service';
import { UserResponse } from '../../model/user.type';
import { EditUserComponent } from '../../components/form/edit-user-form/edit-user.component';
import { Store } from '@ngrx/store';
import { AppState } from '../../state/app.state';
import { Observable } from 'rxjs';
import { userCountSelector, userSelector } from '../../state/user/user.selector';
import { decrementUserCount, removeUser, setUserCount, setUsers } from '../../state/user/user.action';
import { CommonModule } from '@angular/common';
import { AlertService } from '../../service/alert.service';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { UserFormComponent } from '../../components/form/user-from/user-form.component';
import { PasswordFormComponent } from '../../components/form/password-form/password-form.component';
import { DialogComponent } from '../../components/dialog/dialog.component';
import { PaginationComponent } from '../../components/pagination/pagination.component';

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
  
  buffer = signal<UserResponse>({
    firstname: '',
    email: '',
    role: {
      id: 0,
      role: '',
      authorities: []
    },
    editable: false,
    deletable: false
  });
  
  editUser = signal(false);
  createUser = signal(false);
  resetPassword = signal(false);
  confirm = signal(false);
  
  pageCount = signal(0);
  size = signal(2);
  page = signal<Record<string, boolean>> ({
    'prev': false,
    'next': false
  });
  
  users$: Observable<UserResponse[]>;
  userCount$: Observable<number>;
  
  constructor(private store: Store<AppState>) {
    this.users$ = store.select(userSelector);
    this.userCount$ = store.select(userCountSelector);
  }

  ngOnInit(): void {
    this.userService.fetchUserCount().subscribe({
      next: (response) => {
        const res = response as any;
        this.store.dispatch(setUserCount({userCount: res.user_count}));
      }
    })

    this.userService.fetchPagedUsers(this.pageCount(), this.size()).subscribe({
      next: (response) => {
        this.page()['prev'] = response.prev;
        this.page()['next'] = response.next;
        this.store.dispatch(setUsers({users: response.content}));
      }
    });

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

  delete(user: UserResponse) {
    this.buffer.set(user);
    this.toggleConfirm();
  }

  toggleConfirm() {
    this.confirm.update(toggle => !toggle);
  } 

  confirmTrigger(event: boolean) {
    this.toggleConfirm();
    if (event) {
      this.userService.deleteUser(this.buffer().email).subscribe({
        next: (response) => {
          this.store.dispatch(removeUser({email: this.buffer().email}));
          this.store.dispatch(decrementUserCount());
          this.alertService.alert = `User with name ${this.buffer().firstname} deleted`;
        }
      })
    }
  }

  next() {
    this.pageCount.update(count => count + 1);
    this.userService.fetchPagedUsers(this.pageCount(), this.size()).subscribe({
      next: (response) => {
        this.page()['prev'] = response.prev;
        this.page()['next'] = response.next;
        this.store.dispatch(setUsers({users: response.content}));
      }
    });
  }

  prev() {
    this.pageCount.update(count => count - 1);
    this.userService.fetchPagedUsers(this.pageCount(), this.size()).subscribe({
      next: (response) => {
        this.page()['prev'] = response.prev;
        this.page()['next'] = response.next;
        this.store.dispatch(setUsers({users: response.content}));
      }
    });
  }
}
