import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Role } from '../../models/role.enum';
import { CreateTicketComponent } from '../../forms/create-ticket/create-ticket.component';
import { getColor } from '../../shared/utils';
import { jwtDecode } from "jwt-decode";
import { AuthService } from '../../service/auth.service';
import { environment } from '../../../environments/environment';


@Component({
  selector: 'app-home',
  imports: [FontAwesomeModule, RouterOutlet, RouterLink, RouterLinkActive, CreateTicketComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  @ViewChild('navtoggle') navToggle!: ElementRef;

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
    this.toggleSidenav(false);
  }

  toggleSidenav(state: boolean) {
    const toggle = this.navToggle.nativeElement as HTMLInputElement;
    toggle.checked = state;
  }
}
