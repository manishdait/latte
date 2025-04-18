import { Component, inject, OnInit, signal } from '@angular/core';
import { RoleService } from '../../service/role.service';
import { Role } from '../../model/role.enum';
import { RoleFormComponent } from '../../components/form/role-form/role-form.component';
import { PaginationComponent } from '../../components/pagination/pagination.component';

@Component({
  selector: 'app-role',
  imports: [RoleFormComponent],
  templateUrl: './role.component.html',
  styleUrl: './role.component.css'
})
export class RoleComponent implements OnInit {
  roleService = inject(RoleService);

  pageCount = signal(0);
  size = signal(5);
  page = signal<Record<string, boolean>> ({
    'prev': false,
    'next': false
  });

  roles = signal<Role[]>([]);

  createRole = signal(false);

  ngOnInit(): void {
    this.roleService.getRoles().subscribe({
      next: (res) => {this.roles.set(res)}
    })
  }

  toggleCreateRole() {
    this.createRole.update(toggle => !toggle);
  }

  // next() {
  //   this.pageCount.update(count => count + 1);
  //   this.userService.fetchPagedUsers(this.pageCount(), this.size()).subscribe({
  //     next: (response) => {
  //       this.page()['prev'] = response.prev;
  //       this.page()['next'] = response.next;
  //       this.store.dispatch(setUsers({users: response.content}));
  //     }
  //   });
  // }

  // prev() {
  //   this.pageCount.update(count => count - 1);
  //   this.userService.fetchPagedUsers(this.pageCount(), this.size()).subscribe({
  //     next: (response) => {
  //       this.page()['prev'] = response.prev;
  //       this.page()['next'] = response.next;
  //       this.store.dispatch(setUsers({users: response.content}));
  //     }
  //   });
  // }
}
