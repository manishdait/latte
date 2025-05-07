import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TicketFormComponent } from '../../components/form/ticket-form/ticket-form.component';
import { MenubarComponent } from '../../components/menubar/menubar.component';

@Component({
  selector: 'app-home',
  imports: [RouterOutlet, MenubarComponent, TicketFormComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  createTicket = signal(false);

  ngOnInit(): void {}

  createTicketToggle() {
    this.createTicket.update(toggle => !toggle);
  }
}
