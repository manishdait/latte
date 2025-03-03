import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TicketResponse } from '../../models/ticket.type';
import { TicketService } from '../../service/ticket.service';
import { Store } from '@ngrx/store';
import { AppState } from '../../state/app.state';
import { selectTickets } from '../../state/ticket/ticket.selector';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';
import { setTickets } from '../../state/ticket/ticket.action';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { getDate } from '../../shared/utils';

@Component({
  selector: 'app-ticket',
  imports: [RouterLink, CommonModule, FontAwesomeModule],
  templateUrl: './ticket.component.html',
  styleUrl: './ticket.component.css'
})
export class TicketComponent implements OnInit {
  tickets$: Observable<TicketResponse[]>;

  count: number = 0;
  size: number = 10;
  page: Record<string, boolean> = {
    'prev': false,
    'next': false
  }

  constructor(private ticketService: TicketService, private faLibrary: FaIconLibrary, private store: Store<AppState>) {
    this.tickets$ = store.select(selectTickets);
  }

  ngOnInit(): void {
    this.ticketService.fetchPagedTickets(this.count, this.size).subscribe({
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
    this.ticketService.fetchPagedTickets(this.count, this.size).subscribe({
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
}
