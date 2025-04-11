import { Component, ElementRef, inject, OnInit, signal, ViewChild } from '@angular/core';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { TicketService } from '../../service/ticket.service';
import { PatchTicketRequest, TicketResponse } from '../../model/ticket.type';
import { ActivatedRoute, Router } from '@angular/router';
import { getColor, getDate } from '../../shared/utils';
import { Status } from '../../model/status.enum';
import { AuthService } from '../../service/auth.service';
import { Role } from '../../model/role.enum';
import { EditPriorityComponent } from './components/edit-priority/edit-priority.component';
import { EditAssignComponent } from './components/edit-assign/edit-assign.component';
import { ActivityComponent } from '../../components/activity/activity.component';
import { CommentRequest } from '../../model/comment.type';
import { CommentService } from '../../service/comment.service';
import { Priority } from '../../model/priority.enum';

@Component({
  selector: 'app-ticket-details',
  imports: [FontAwesomeModule, ActivityComponent, EditAssignComponent, EditPriorityComponent],
  templateUrl: './ticket-details.component.html',
  styleUrl: './ticket-details.component.css'
})
export class TicketDetailsComponent implements OnInit {
  @ViewChild('activity') activity!: ActivityComponent;
  @ViewChild('title') titleField!: ElementRef;

  authService = inject(AuthService);
  ticketService = inject(TicketService);
  commentService = inject(CommentService);
  router = inject(Router)
  route = inject(ActivatedRoute);
  faLibrary = inject(FaIconLibrary);

  ticket = signal<TicketResponse>({
    id: 0,
    title: '',
    description: '',
    priority: Priority.LOW,
    status: Status.OPEN,
    lock: false,
    createdBy: {
      firstname: '',
      email: '',
      role: Role.USER
    },
    assignedTo: null,
    createdAt: new Date(),
    lastUpdated: new Date()
  });

  ticketId = signal(this.route.snapshot.params['id']);
  
  editTitle = signal(false);
  editAssignee = signal(false);
  editPriority = signal(false);
  admin = signal(this.authService.getRole() === Role.ADMIN);

  ngOnInit(): void {
    this.ticketService.fetchTicket(this.ticketId()).subscribe({
      next: (response) => {
        this.ticket.set(response);
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

  getStatus(status: Status) {
    return status.toString();
  }

  getAssignee() {
    if(this.ticket().assignedTo && this.ticket().assignedTo !== null) {
      return this.ticket().assignedTo?.firstname;
    }
    return '';
  }

  getPriority() {
    if (this.ticket().priority === Priority.LOW) {
      return 'Low';
    } else if (this.ticket().priority === Priority.MEDIUM) {
      return 'Medium';
    } else {
      return 'High';
    }
  }

  refresh() {
    this.ngOnInit();
    this.activity.ngOnInit();
  }

  updateStatus() {
    if (this.ticket) {
      const request: PatchTicketRequest = {
        title: null,
        description: null,
        priority: null,
        status: this.ticket().status === Status.OPEN? Status.CLOSE : Status.OPEN,
        assignedTo: null
      }

      this.ticketService.updateTicket(this.ticketId(), request).subscribe({
        next: (response) => {
          this.ticket.set(response);
          this.activity.ngOnInit(); 
        }
      }) 
    }
  }

  lockTicket() {
    this.ticketService.lockTicket(this.ticketId()).subscribe({
      next: (response) => {
        this.ticket.set(response);
        this.activity.ngOnInit(); 
      }
    }) 
  }

  unlockTicket() {
    this.ticketService.unlockTicket(this.ticketId()).subscribe({
      next: (response) => {
        this.ticket.set(response);
        this.activity.ngOnInit(); 
      }
    })
  }

  updateTitle() {
    let title = (this.titleField.nativeElement as HTMLInputElement).value;

    if (title === '' || title === this.ticket().title) {return;}
    const request: PatchTicketRequest = {
      title: title,
      description: null,
      priority: null,
      status: null,
      assignedTo: null
    }

    this.ticketService.updateTicket(this.ticketId(), request).subscribe({
      next: (response) => {
        this.toggleEditTitle();
        this.ticket.set(response);
        this.activity.ngOnInit();
      }
    })
  }

  toggleEditTitle() {
    this.editTitle.update(toggle => !toggle);
  }

  toggleEditAssignee() {
    this.editAssignee.update(toggle => !toggle);
  }

  toggleEditPriority() {
    this.editPriority.update(toggle => !toggle);
  }

  comment(message: HTMLTextAreaElement) {
    if (message.value !== '') {
      const request: CommentRequest = {
        ticketId: this.ticketId(),
        message: message.value
      }

      this.commentService.createComment(request).subscribe({
        next: () => {
          message.value = '';
          this.activity.ngOnInit()
        }
      })
    }
  }
}
