import { Component, input } from '@angular/core';
import { TicketResponse } from '../../model/ticket.type';
import { getDate } from '../../shared/utils';

@Component({
  selector: 'app-list-item',
  imports: [],
  templateUrl: './list-item.component.html',
  styleUrl: './list-item.component.css'
})
export class ListItemComponent {
  ticket = input.required<TicketResponse>();

  getDate(date: any) {
    return getDate(date);
  }
}
