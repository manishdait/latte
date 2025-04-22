import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { DropdownComponent } from '../../dropdown/dropdown.component';
import { RoleService } from '../../../service/role.service';
import { Role } from '../../../model/role.type';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { AppState } from '../../../state/app.state';
import { removeRole, updateRoleCount } from '../../../state/role/role.action';

@Component({
  selector: 'app-role-delete',
  imports: [ReactiveFormsModule, DropdownComponent],
  templateUrl: './role-delete.component.html',
  styleUrl: './role-delete.component.css'
})
export class RoleDeleteComponent implements OnInit {
  toDelete = input.required<number>();
  cancel = output<boolean>();
  
  roleService = inject(RoleService);

  page = signal(0);
  size = signal(5);
  roles = signal<Role[]>([]);
  list = signal<string[]>([]);
  hasNext = signal(false);

  role = new FormControl('Admin', [Validators.required]);

  constructor(private store: Store<AppState>) {}

  ngOnInit(): void {
    this.getRoles();
  }

  getNext() {
    this.page.update(count => count + 1);
    this.getRoles();
  }

  getRoles() {
    this.roleService.getRoles(this.page(), this.size()).subscribe({
      next: (res) => {
        this.roles.update(role => role.concat(res.content));
        this.list.set(this.getRoleList());
        this.hasNext.set(res.next);
      }
    });
  }

  onSubmit() {
    const selectedRole = this.role.value;
    const roleId = this.roles().find(role => role.role === selectedRole)?.id;
    this.roleService.deleteRole(this.toDelete(), roleId ?? 0).subscribe({
      next: (res) => {
        this.store.dispatch(removeRole({roleId: this.toDelete()}));
        this.store.dispatch(updateRoleCount({count: -1}));
        this.cancel.emit(true);
      }
    });
  }

  getRoleList(): string[] {
    return this.roles().filter(role => role.id !== this.toDelete()).map(role => role.role);
  }
}
