import { Component, inject, OnInit, output, signal } from '@angular/core';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { AuthService } from '../../service/auth.service';
import { Role } from '../../model/role.enum';
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
  admin = signal(this.authService.getRole() === Role.ADMIN);

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  toggleAdminDropDown() {
    this.adminDropDownToggle.update(toggle => !toggle);
  }

  toggleCreateTopic() {
    this.createTicket.emit(true);
  }
}
