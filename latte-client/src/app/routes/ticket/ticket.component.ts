import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { Status } from '../../model/status.enum';
import { TicketResponse } from '../../model/ticket.type';
import { TicketService } from '../../service/ticket.service';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { getDate } from '../../shared/utils';
import { AppState } from '../../state/app.state';
import { setTicketOpenCount, setTicketCloseCount, setTickets } from '../../state/ticket/ticket.action';
import { selectTickets, selectTicketOpenCount, selectTicketCloseCount } from '../../state/ticket/ticket.selector';

@Component({
  selector: 'app-ticket',
  imports: [RouterLink, CommonModule, FontAwesomeModule],
  templateUrl: './ticket.component.html',
  styleUrl: './ticket.component.css'
})
export class TicketComponent implements OnInit {
  tickets$: Observable<TicketResponse[]>;

  openCount$: Observable<number>;
  closeCount$: Observable<number>;

  status: Status = Status.OPEN;

  count: number = 0;
  size: number = 10;
  page: Record<string, boolean> = {
    'prev': false,
    'next': false
  }

  constructor(private ticketService: TicketService, private faLibrary: FaIconLibrary, private store: Store<AppState>) {
    this.tickets$ = store.select(selectTickets);
    this.openCount$ = store.select(selectTicketOpenCount);
    this.closeCount$ = store.select(selectTicketCloseCount);
  }

  ngOnInit(): void {
    this.ticketService.fetchTicktsInfo().subscribe({
      next: (response) => {
        const info =  response as any;
        this.store.dispatch(setTicketOpenCount({ticketCount: info.open_tickets}))
        this.store.dispatch(setTicketCloseCount({ticketCount: info.completed_tickets}))
      },
      error: (err) => {
        console.error(err.error);
      }
    })

    this.ticketService.fetchPagedTicketsByStaus(this.status, this.count, this.size).subscribe({
      next: (response) => {
        this.page['prev'] = response.prev;
        this.page['next'] = response.next;

        this.store.dispatch(setTickets({tickets: response.content}))
      }
    });

    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  next() {
    this.count += 1;
    this.ticketService.fetchPagedTickets(this.count, this.size).subscribe({
      next: (response) => {
        this.page['prev'] = response.prev;
        this.page['next'] = response.next;

        this.store.dispatch(setTickets({tickets: response.content}))
      }
    });
  }

  prev() {
    this.count -= 1;
    this.ticketService.fetchPagedTicketsByStaus(this.status, this.count, this.size).subscribe({
      next: (response) => {
        this.page['prev'] = response.prev;
        this.page['next'] = response.next;

        this.store.dispatch(setTickets({tickets: response.content}))
      }
    });
  }

  getDate(date: any) {
    return getDate(date);
  }

  getStatus() {
    return this.status.toString()
  }

  setStatus(status: string) {
    if(status === 'OPEN'){
      this.status = Status.OPEN;
    } else {
      this.status = Status.CLOSE;
    }
    this.ngOnInit();
  }
}
