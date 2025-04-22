import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../service/auth.service';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { generateColor } from '../../shared/utils';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, FontAwesomeModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {
  authService = inject(AuthService);
  faLibrary = inject(FaIconLibrary);

  sidenavState = input(false)
  sidenavToggle = output<boolean>();
  
  version = signal(environment.VERSION);
  username = signal(this.authService.getFirstname());
  
  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  getColor(username: any): string {
    if (!username) {return '#ddd'}
    return generateColor(username);
  }
}
