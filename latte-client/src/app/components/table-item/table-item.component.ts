import { Component, inject, input, OnInit } from '@angular/core';
import { TicketResponse } from '../../model/ticket.type';
import { getDate } from '../../shared/utils';
import { Observable } from 'rxjs';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { CommonModule } from '@angular/common';
import { Status } from '../../model/status.type';
import { Priorities } from '../../model/priority.type';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-table-item',
  imports: [CommonModule, RouterLink, FontAwesomeModule],
  templateUrl: './table-item.component.html',
  styleUrl: './table-item.component.css'
})
export class TableItemComponent implements OnInit {
  tickets$ = input.required<Observable<TicketResponse[]>>();
  
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
