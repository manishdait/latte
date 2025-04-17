import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { getColor } from '../../shared/utils';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../service/auth.service';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, FontAwesomeModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {
  authService = inject(AuthService);
  faLibrary = inject(FaIconLibrary);

  sidenavToggle = input(false)
  sidenav = output<boolean>();
  
  version = signal(environment.VERSION);
  username = signal(this.authService.getFirstname());
  
  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  color(username: any): string {
    if (!username) {return '#ddd'}
    return getColor(username);
  }
}
