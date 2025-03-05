import { Component, OnInit } from '@angular/core';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Role } from '../../model/role.enum';
import { getColor } from '../../shared/utils';
import { jwtDecode } from "jwt-decode";
import { AuthService } from '../../service/auth.service';
import { environment } from '../../../environments/environment';
import { TicketFormComponent } from '../../components/form/ticket-form/ticket-form.component';

@Component({
  selector: 'app-home',
  imports: [FontAwesomeModule, RouterOutlet, RouterLink, RouterLinkActive, TicketFormComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  version: string = environment.VERSION;
  
  role: Role;
  firstname: string;

  ticketToggle: boolean = false;

  constructor(private faLibrary: FaIconLibrary, private authService: AuthService) {
    this.role = authService.getRoles();
    const token:any = jwtDecode(authService.getAccessToken());
    this.firstname = token.firstname;
  }

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  color(username: any): string {
    if (!username) {return '#ddd'}
    return getColor(username);
  }

  isAdmin(): boolean {
    return this.role === Role.ADMIN;
  }

  createTicket() {
    this.ticketToggle = true;
  }
}
