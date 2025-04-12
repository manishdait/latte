import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { Status } from '../../model/status.enum';
import { TicketResponse } from '../../model/ticket.type';
import { TicketService } from '../../service/ticket.service';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { AppState } from '../../state/app.state';
import { setTicketOpenCount, setTicketCloseCount, setTickets } from '../../state/ticket/ticket.action';
import { selectTickets, selectTicketOpenCount, selectTicketCloseCount } from '../../state/ticket/ticket.selector';
import { PaginationComponent } from '../../components/pagination/pagination.component';
import { ListItemComponent } from '../../components/list-item/list-item.component';

@Component({
  selector: 'app-ticket',
  imports: [PaginationComponent, ListItemComponent, RouterLink, CommonModule, FontAwesomeModule],
  templateUrl: './ticket.component.html',
  styleUrl: './ticket.component.css'
})
export class TicketComponent implements OnInit {
  ticketService = inject(TicketService);
  faLibrary = inject(FaIconLibrary);

  tickets$: Observable<TicketResponse[]>;

  openCount$: Observable<number>;
  closeCount$: Observable<number>;

  status = signal(Status.OPEN);
  pageCount = signal(0);
  size = signal(10);
  page = signal<Record<string, boolean>>({
    'prev': false,
    'next': false
  });

  constructor(private store: Store<AppState>) {
    this.tickets$ = store.select(selectTickets);
    this.openCount$ = store.select(selectTicketOpenCount);
    this.closeCount$ = store.select(selectTicketCloseCount);
  }

  ngOnInit(): void {
    this.ticketService.fetchTicktsInfo().subscribe({
      next: (response) => {
        const info =  response as any;
        this.store.dispatch(setTicketOpenCount({ticketCount: info.open_tickets}))
        this.store.dispatch(setTicketCloseCount({ticketCount: info.closed_tickets}))
      },
      error: (err) => {
        console.error(err.error);
      }
    })

    this.ticketService.fetchPagedTicketsByStaus(this.status(), this.pageCount(), this.size()).subscribe({
      next: (response) => {
        this.page()['prev'] = response.prev;
        this.page()['next'] = response.next;

        this.store.dispatch(setTickets({tickets: response.content}))
      }
    });

    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  next() {
    this.pageCount.update(count => count + 1);
    this.ticketService.fetchPagedTickets(this.pageCount(), this.size()).subscribe({
      next: (response) => {
        this.page()['prev'] = response.prev;
        this.page()['next'] = response.next;

        this.store.dispatch(setTickets({tickets: response.content}))
      }
    });
  }

  prev() {
    this.pageCount.update(count => count - 1);
    this.ticketService.fetchPagedTicketsByStaus(this.status(), this.pageCount(), this.size()).subscribe({
      next: (response) => {
        this.page()['prev'] = response.prev;
        this.page()['next'] = response.next;

        this.store.dispatch(setTickets({tickets: response.content}))
      }
    });
  }

  getStatus() {
    return this.status.toString()
  }

  setStatus(status: string) {
    if(status === 'OPEN'){
      this.status.set(Status.OPEN);
    } else {
      this.status.set(Status.CLOSE);
    }
    this.ngOnInit();
  }
}
