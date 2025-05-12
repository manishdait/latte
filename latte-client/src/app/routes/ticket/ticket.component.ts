import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { Status } from '../../model/status.type';
import { TicketResponse } from '../../model/ticket.type';
import { TicketService } from '../../service/ticket.service';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { AppState } from '../../state/app.state';
import { setCloseCount, setOpenCount, setTicketCount, setTickets } from '../../state/ticket/ticket.action';
import { closeTickets, openTickets, tickets } from '../../state/ticket/ticket.selector';
import { PaginationComponent } from '../../components/pagination/pagination.component';
import { TableItemComponent } from '../../components/table-item/table-item.component';
import { CardItemComponent } from '../../components/card-item/card-item.component';

@Component({
  selector: 'app-ticket',
  imports: [PaginationComponent, TableItemComponent, CardItemComponent, RouterLink, CommonModule, FontAwesomeModule],
  templateUrl: './ticket.component.html',
  styleUrl: './ticket.component.css'
})
export class TicketComponent implements OnInit {
  ticketService = inject(TicketService);
  faLibrary = inject(FaIconLibrary);

  tickets$: Observable<TicketResponse[]>;

  openCount$: Observable<number>;
  closeCount$: Observable<number>;

  availableStatus = ['All Tickets', 'Open Tickets', 'Closed Tickets'];
  status = signal<string>('All Tickets');

  page = signal(0);
  size = signal(10);
  
  ticketPage = signal<Record<string, boolean>>({
    'prev': false,
    'next': false
  });

  loading = signal(true);
  statusFilter = signal(false);

  constructor(private store: Store<AppState>) {
    this.tickets$ = store.select(tickets);
    this.openCount$ = store.select(openTickets);
    this.closeCount$ = store.select(closeTickets);
  }

  ngOnInit(): void {
    this.ticketService.fetchTicktsInfo().subscribe({
      next: (response) => {
        const info =  response as any;
        this.store.dispatch(setOpenCount({count: info.open_tickets}))
        this.store.dispatch(setCloseCount({count: info.closed_tickets}))
        this.store.dispatch(setTicketCount({count: info.open_tickets + info.closed_tickets}))
      }
    })

    this.getTickets();
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  getNext() {
    this.page.update(count => count + 1);
    this.getTickets();
  }

  getPrev() {
    this.page.update(count => count - 1);
    this.getTickets();
  }

  getStatus() {
    return this.status.toString()
  }

  setStatus(status: string) {
    this.status.set(status);
    this.toggleStatusFilter();
    this.ngOnInit();
  }

  toggleStatusFilter() {
    this.statusFilter.update(toggle => !toggle);
  }

  getTickets() {
    this.loading.set(true);
    if (this.status() === 'All Tickets') {
      this.ticketService.fetchPagedTickets(this.page(), this.size()).subscribe({
        next: (response) => {
          this.ticketPage()['prev'] = response.prev;
          this.ticketPage()['next'] = response.next;
  
          this.store.dispatch(setTickets({tickets: response.content}));
          this.loading.set(false);
        }
      });
    } else {
      const status: Status = this.status() === 'Open Tickets'? 'OPEN' : 'CLOSE';
      this.ticketService.fetchPagedTicketsByStaus(status, this.page(), this.size()).subscribe({
        next: (response) => {
          this.ticketPage()['prev'] = response.prev;
          this.ticketPage()['next'] = response.next;
  
          this.store.dispatch(setTickets({tickets: response.content}));
          this.loading.set(false);
        }
      });
    }
  }
}
