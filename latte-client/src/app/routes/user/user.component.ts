import { Component, OnInit } from '@angular/core';
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

@Component({
  selector: 'app-user',
  imports: [UserFormComponent, EditUserComponent, PasswordFormComponent, CommonModule, DialogComponent, FontAwesomeModule],
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

  userCount$: Observable<number>;

  count: number = 0;
  size: number = 10;
  page: Record<string, boolean> = {
    'prev': false,
    'next': false
  }
  constructor(private userService: UserService, private alertService: AlertService, private faLibrary: FaIconLibrary, private store: Store<AppState>) {
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
          this.store.dispatch(removeUser({email: this.user!.email}));
          this.store.dispatch(decrementUserCount());
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
