import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { TicketService } from '../../service/ticket.service';
import { PatchTicketRequest, TicketResponse } from '../../models/ticket.type';
import { ActivatedRoute, Router } from '@angular/router';
import { getColor, getDate } from '../../shared/utils';
import { ActivityComponent } from '../../components/activity/activity.component';
import { EditAssignComponent } from '../../components/edit-assign/edit-assign.component';
import { Status } from '../../models/status.enum';
import { EditPriorityComponent } from '../../components/edit-priority/edit-priority.component';
import { AuthService } from '../../service/auth.service';
import { Role } from '../../models/role.enum';

@Component({
  selector: 'app-ticket-details',
  imports: [FontAwesomeModule, ActivityComponent, EditAssignComponent, EditPriorityComponent],
  templateUrl: './ticket-details.component.html',
  styleUrl: './ticket-details.component.css'
})
export class TicketDetailsComponent implements OnInit {
  @ViewChild('desc') descInput!: ElementRef;
  @ViewChild('activity') activity!: ActivityComponent;

  ticket: TicketResponse | undefined;
  ticketId: number;
  assignToggle: boolean = false;

  descriptionChanged:boolean = false;
  description: string = '';

  deleteToggle: boolean = false;
  priorityToggle: boolean = false;

  constructor (private faLibrary: FaIconLibrary, private authService: AuthService, private ticketService: TicketService, private router: Router, private route: ActivatedRoute) {
    this.ticketId = route.snapshot.params['id'];
  }

  ngOnInit(): void {
    this.ticketService.fetchTicket(this.ticketId).subscribe({
      next: (response) => {
        this.ticket = response;
        this.description = response.description;
      }
    });

    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  color(username: any): string {
    if (!username) {return '#ddd'}
    return getColor(username);
  }

  getDate(date: any) {
    return getDate(date);
  }

  detectDescription(value: string) {
    this.description = value;
    this.descriptionChanged = this.ticket!.description !== value;
  }

  editDescription() {
    const request: PatchTicketRequest = {
      title: null,
      description: this.description,
      priority: null,
      status: null,
      assignedTo: null
    }

    if (this.ticket && this.description) {
      this.ticketService.updateTicket(this.ticket.id, request).subscribe((response) => {
        this.ticket = response;
        this.activity.ngOnInit();
      });
    }

    this.descriptionChanged = false;
  }

  cancleDescrChanges() {
    this.descriptionChanged = false;
    const input = this.descInput.nativeElement as HTMLInputElement;
    input.value = this.ticket!.description;
  }

  delete() {
    if (this.ticket) {
      this.ticketService.deleteTicket(this.ticket.id).subscribe({
        next: (response) => {
          console.log(response);
          this.router.navigate(['home/tickets'], {replaceUrl: true});
        }
      })
    }
  }

  refreshActivities() {
    this.ngOnInit();
    this.activity.ngOnInit();
  }

  getStatus(status: Status) {
    return status.toString();
  }

  updateStatus() {
    if (this.ticket) {
      const request: PatchTicketRequest = {
        title: null,
        description: null,
        priority: null,
        status: this.ticket.status === Status.OPEN? Status.CLOSE : Status.OPEN,
        assignedTo: null
      }

      this.ticketService.updateTicket(this.ticketId, request).subscribe({
        next: (response) => {
          this.ticket = response;
          this.activity.ngOnInit(); 
        }
      }) 
    }
  }

  lockTicket() {
    this.ticketService.lockTicket(this.ticketId).subscribe({
      next: (response) => {
        this.ticket = response;
        this.activity.ngOnInit(); 
      }
    }) 
  }

  unlockTicket() {
    this.ticketService.unlockTicket(this.ticketId).subscribe({
      next: (response) => {
        this.ticket = response;
        this.activity.ngOnInit(); 
      }
    })
  }

  isAdmin() {
    return this.authService.getRoles() === Role.ADMIN;
  }
}
