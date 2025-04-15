import { Component, inject, OnInit, output, signal } from '@angular/core';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { AuthService } from '../../service/auth.service';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-side-navbar',
  imports: [FontAwesomeModule, RouterLink, RouterLinkActive],
  templateUrl: './side-navbar.component.html',
  styleUrl: './side-navbar.component.css'
})
export class SideNavbarComponent implements OnInit {
  createTicket = output<boolean>();

  faLibrary = inject(FaIconLibrary);
  authService = inject(AuthService);

  adminDropDownToggle = signal(false);

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  toggleAdminDropDown() {
    this.adminDropDownToggle.update(toggle => !toggle);
  }

  toggleCreateTopic() {
    this.createTicket.emit(true);
  }

  createTicketOps() {
    return this.authService.createTicket();
  }

  admin() {
    return  this.userOps() || this.roleOps(); 
  }

  userOps() {
    return this.authService.createUser() || this.authService.deleteUser() || this.authService.editUser() || this.authService.resetUserPassword();
  }

  roleOps() {
    return this.authService.createRole() || this.authService.editRole() || this.authService.deleteRole();
  }
}
