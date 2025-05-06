import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { AuthService } from '../../service/auth.service';
import { TicketService } from '../../service/ticket.service';
import { generateColor } from '../../shared/utils';
import { AppState } from '../../state/app.state';
import { setCloseCount, setOpenCount, setTicketCount } from '../../state/ticket/ticket.action';
import { closeTickets, openTickets, totalTickets } from '../../state/ticket/ticket.selector';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FontAwesomeModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  authService = inject(AuthService);
  ticketService = inject(TicketService);

  username = signal(this.authService.getFirstname());

  openTickets$: Observable<number>;
  closedTickets$: Observable<number>;
  totalTickets$: Observable<number>;

  constructor (private store: Store<AppState>) {
    this.totalTickets$ = store.select(totalTickets);
    this.openTickets$ = store.select(openTickets);
    this.closedTickets$ = store.select(closeTickets);
  }

  ngOnInit(): void {
    this.ticketService.fetchTicktsInfo().subscribe({
      next: (res) => {
        this.store.dispatch(setOpenCount({count: res['open_tickets']}));
        this.store.dispatch(setCloseCount({count: res['closed_tickets']}));
        this.store.dispatch(setTicketCount({count: res['total_tickets']}));
      }
    });
  }

  getColor(username: any): string {
    if (!username) {return '#ddd'}
    return generateColor(username);
  }

  greet(): string {
    const hours: number = new Date().getHours();
    if (hours >= 5 && hours <= 11) {
      return "Good Morning";
    } else if (hours >= 12 && hours <= 16) {
      return "Good Afternoon";
    } else {
      return "Good Evening";
    }
  }
}
