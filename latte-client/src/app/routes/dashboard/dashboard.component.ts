import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { AuthService } from '../../service/auth.service';
import { TicketService } from '../../service/ticket.service';
import { getDate, greet } from '../../shared/utils';
import { AppState } from '../../state/app.state';
import { setCloseCount, setOpenCount, setTicketCount } from '../../state/ticket/ticket.action';
import { closeTickets, openTickets, totalTickets } from '../../state/ticket/ticket.selector';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TicketResponse } from '../../model/ticket.type';
import { CardItemComponent } from '../../components/card-item/card-item.component';
import { RouterLink } from '@angular/router';
import { ShimmerComponent } from '../../components/shimmer/shimmer.component';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink, FontAwesomeModule, CardItemComponent, ShimmerComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  authService = inject(AuthService);
  ticketService = inject(TicketService);

  user = signal(this.authService.user);
  greet = signal(greet());

  openTickets$: Observable<number>;
  closedTickets$: Observable<number>;
  totalTickets$: Observable<number>;

  tickets = signal<TicketResponse[]>([]);

  loading = signal(false);

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
    this.loading.set(true);
    this.ticketService.fetchPagedTickets(0, 5).subscribe({
      next: (res) => {
        this.tickets.set(res.content);
        this.store.dispatch(setTicketCount({count: res.totalElement}))
        this.loading.set(false);
      }
    });  
  }

  getDate(date: any) {
    return getDate(date);
  }
}
