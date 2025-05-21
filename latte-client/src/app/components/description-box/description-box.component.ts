import { Component, ElementRef, inject, input, output, signal, ViewChild } from '@angular/core';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { AuthService } from '../../service/auth.service';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { getDate } from '../../shared/utils';
import { TicketService } from '../../service/ticket.service';
import { PatchTicketRequest, TicketResponse } from '../../model/ticket.type';
import { Authority } from '../../model/role.type';

@Component({
  selector: 'app-description-box',
  imports: [FontAwesomeModule],
  templateUrl: './description-box.component.html',
  styleUrl: './description-box.component.css'
})
export class DescriptionBoxComponent {
  @ViewChild('description') description!: ElementRef;

  authService = inject(AuthService);
  faLibrary = inject(FaIconLibrary);
  ticketService = inject(TicketService);
  
  ticket = input.required<TicketResponse>();
  owner = input(false);
  refresh = output<boolean>();

  util = signal(false);
  edit = signal(false);
  
  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  toggleUtil() {
    this.util.update(toggle => !toggle);
  }

  toggleEdit() {
    this.edit.update(toggle => !toggle);
  }

  updateComment() {
    const description = (this.description.nativeElement as HTMLTextAreaElement).value;
    if (description === this.ticket().description) {
      this.edit.set(false);
      return;
    }

    const request: PatchTicketRequest = {
      title: null,
      description: description,
      priority: null,
      status: null,
      assignedTo: null,
      clientId: null
    }

    this.ticketService.updateTicket(this.ticket().id, request).subscribe({
      next: (response) => {
        this.edit.set(false);
        this.refresh.emit(true);
      },
      error: (err) => {
        console.error(err);
      }
    })
  }

  hasAuthority(authority: Authority) {
    return this.authService.user.role.authorities.includes(authority);
  }

  getDate(date: any) {
    return getDate(date);
  }
}
