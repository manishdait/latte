import { Component, inject, input, OnInit } from '@angular/core';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { Priorities } from '../../model/priority.type';
import { Status } from '../../model/status.type';
import { TicketResponse } from '../../model/ticket.type';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { getDate } from '../../shared/utils';

@Component({
  selector: 'app-card-item',
  imports: [],
  templateUrl: './card-item.component.html',
  styleUrl: './card-item.component.css'
})
export class CardItemComponent implements OnInit {
  ticket = input.required<TicketResponse>();
  
  faLibrary = inject(FaIconLibrary);

  priority = Priorities;
  status = Status;

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  getDate(date: any) {
    return getDate(date);
  }
}
