import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TicketFormComponent } from '../../components/form/ticket-form/ticket-form.component';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { SideNavbarComponent } from '../../components/side-navbar/side-navbar.component';

@Component({
  selector: 'app-home',
  imports: [RouterOutlet, NavbarComponent, SideNavbarComponent, TicketFormComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  createTicket = signal(false);
  sidenav = signal(false);

  ngOnInit(): void {}

  createTicketToggle() {
    this.createTicket.update(toggle => !toggle);
  }

  toggleSidenav() {
    this.sidenav.update(toggle => !toggle);
  }
}
