import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { Store } from '@ngrx/store';
import { jwtDecode } from 'jwt-decode';
import { Observable } from 'rxjs';
import { AuthService } from '../../service/auth.service';
import { TicketService } from '../../service/ticket.service';
import { getColor } from '../../shared/utils';
import { AppState } from '../../state/app.state';
import { setTicketOpenCount, setTicketCloseCount } from '../../state/ticket/ticket.action';
import { selectTicketOpenCount, selectTicketCloseCount } from '../../state/ticket/ticket.selector';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  authService = inject(AuthService);
  ticketService = inject(TicketService);

  username = signal(this.authService.getFirstname());

  openTickets$: Observable<number>;
  closedTickets$: Observable<number>;
  totalTickets = signal(0);

  constructor (private store: Store<AppState>) {
    this.openTickets$ = store.select(selectTicketOpenCount);
    this.closedTickets$ = store.select(selectTicketCloseCount);
  }

  ngOnInit(): void {
    this.ticketService.fetchTicktsInfo().subscribe({
      next: (response) => {
        let open = response['open_tickets'] ?? 0;
        let close = response['closed_tickets'] ?? 0;

        this.store.dispatch(setTicketOpenCount({ticketCount: open}));
        this.store.dispatch(setTicketCloseCount({ticketCount: close}));
        this.totalTickets.set(open + close);
      },
      error: (err) => {
        console.error(err.error);
      }
    });
  }

  color(username: any): string {
    if (!username) {return '#ddd'}
    return getColor(username);
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
