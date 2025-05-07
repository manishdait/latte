import { Component, inject, input, OnInit } from '@angular/core';
import { TicketResponse } from '../../model/ticket.type';
import { getDate } from '../../shared/utils';
import { Observable } from 'rxjs';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { CommonModule } from '@angular/common';
import { Status } from '../../model/status.type';
import { Priority } from '../../model/priority.type';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-list-item',
  imports: [FontAwesomeModule, CommonModule, RouterLink],
  templateUrl: './list-item.component.html',
  styleUrl: './list-item.component.css'
})
export class ListItemComponent implements OnInit {
  tickets$ = input.required<Observable<TicketResponse[]>>();
  
  faLibrary = inject(FaIconLibrary);

  priority: Record<Priority, string> = {
    'LOW': 'Low',
    'MEDIUM': 'Medium',
    'HIGH': 'High'
  }

  status: Record<Status, string> = {
    'OPEN': 'Open',
    'CLOSE': 'Close'
  }

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
    
  }

  getDate(date: any) {
    return getDate(date);
  }
}
