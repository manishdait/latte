import { Component, inject, OnInit, signal } from '@angular/core';
import { RoleService } from '../../service/role.service';
import { Role } from '../../model/role.type';
import { RoleFormComponent } from '../../components/form/role-form/role-form.component';
import { PaginationComponent } from '../../components/pagination/pagination.component';
import { AuthService } from '../../service/auth.service';
import { EditRoleFormComponent } from '../../components/form/edit-role-form/edit-role-form.component';
import { RoleDeleteComponent } from '../../components/form/role-delete/role-delete.component';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';
import { AppState } from '../../state/app.state';
import { roleCount, roles } from '../../state/role/role.selector';
import { setRoleCount, setRoles } from '../../state/role/role.action';
import { CommonModule } from '@angular/common';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { HasAuthorityDirective } from '../../directives/has-autority.directive';
import { ShimmerComponent } from '../../components/shimmer/shimmer.component';

@Component({
  selector: 'app-role',
  imports: [CommonModule, FontAwesomeModule, RoleFormComponent, EditRoleFormComponent, RoleDeleteComponent, PaginationComponent, ShimmerComponent, HasAuthorityDirective],
  templateUrl: './role.component.html',
  styleUrl: './role.component.css'
})
export class RoleComponent implements OnInit {
  roleService = inject(RoleService);
  authService = inject(AuthService);
  faLibrary = inject(FaIconLibrary);

  page = signal(0);
  size = signal(10);
  rolePage = signal<Record<string, boolean>> ({
    'prev': false,
    'next': false
  });
  
  loading = signal(true);
  bufferId = signal(0);
  
  createRole = signal(false);
  editRole = signal(false);
  deleteRole = signal(false);
  
  count$: Observable<number>;
  roles$: Observable<Role[]>;

  constructor(private store: Store<AppState>) {
    this.count$ = store.select(roleCount);
    this.roles$ = store.select(roles);
  }

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
    
    this.roleService.getCount().subscribe({
      next: (res) => {
        this.store.dispatch(setRoleCount({count: res['role_count']}))
      }
    });
    
    this.getRoles();
  }

  toggleCreateRole() {
    this.createRole.update(toggle => !toggle);
  }

  toggleEditRole(id: number) {
    this.bufferId.set(id);
    this.editRole.update(toggle => !toggle);
  }

  toggleDeleteRole(id: number) {
    this.bufferId.set(id);
    this.deleteRole.update(toggle => !toggle);
  }

  getNext() {
    this.page.update(count => count + 1);
    this.getRoles()
  }

  getPrev() {
    this.page.update(count => count - 1);
    this.getRoles()
  }

  getRoles() {
    this.loading.set(true);
    this.roleService.getRoles(this.page(), this.size()).subscribe({
      next: (res) => {
        this.store.dispatch(setRoles({roles: res.content}))
        this.rolePage()['prev'] = res.prev;
        this.rolePage()['next'] = res.next;
        this.loading.set(false);
      }
    });
  }
}
