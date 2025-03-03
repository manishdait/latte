import { Component, OnInit } from '@angular/core';
import { getColor } from '../../shared/utils';
import { jwtDecode } from 'jwt-decode';
import { AuthService } from '../../service/auth.service';
import { TicketService } from '../../service/ticket.service';
import { Store } from '@ngrx/store';
import { setTicketCloseCount, setTicketOpenCount } from '../../state/ticket/ticket.action';
import { Observable } from 'rxjs';
import { selectTicketCloseCount, selectTicketOpenCount } from '../../state/ticket/ticket.selector';
import { AppState } from '../../state/app.state';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  firstname: string;

  openCount$: Observable<number>;
  closeCount$: Observable<number>;

  constructor (private authService: AuthService, private ticketServie: TicketService, private store: Store<AppState>) {
    const token:any = jwtDecode(authService.getAccessToken());
    this.firstname = token.firstname;

    this.openCount$ = store.select(selectTicketOpenCount);
    this.closeCount$ = store.select(selectTicketCloseCount);
  }

  ngOnInit(): void {
    this.ticketServie.fetchTicktsInfo().subscribe({
      next: (response) => {
        const info =  response as any;
        this.store.dispatch(setTicketOpenCount({ticketCount: info.open_tickets}))
        this.store.dispatch(setTicketCloseCount({ticketCount: info.completed_tickets}))
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
