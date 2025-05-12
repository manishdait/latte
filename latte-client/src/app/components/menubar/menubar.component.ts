import { Component, inject, OnInit, output, signal } from '@angular/core';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { HasAuthorityDirective } from '../../shared/directives/has-autority.directive';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-menubar',
  imports: [FontAwesomeModule, RouterLinkActive, RouterLink, HasAuthorityDirective],
  templateUrl: './menubar.component.html',
  styleUrl: './menubar.component.css'
})
export class MenubarComponent implements OnInit {
  createTicket = output<boolean>();

  authService = inject(AuthService);
  router = inject(Router);
  faLibrary = inject(FaIconLibrary);

  sidenav = signal(false);

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  toggleSidenav() {
    this.sidenav.update(toggle => !toggle);
  }

  toggleCreateTicket() {
    this.createTicket.emit(true);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/sign-in'], {replaceUrl: true});
  }
}
