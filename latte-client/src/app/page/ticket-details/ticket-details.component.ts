import { Component, ElementRef, inject, OnInit, signal, ViewChild } from '@angular/core';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { TicketService } from '../../service/ticket.service';
import { PatchTicketRequest, TicketResponse } from '../../model/ticket.type';
import { ActivatedRoute, Router } from '@angular/router';
import { generateColor, getDate } from '../../shared/utils';
import { Status } from '../../model/status.type';
import { AuthService } from '../../service/auth.service';
import { EditPriorityComponent } from './components/edit-priority/edit-priority.component';
import { EditAssignComponent } from './components/edit-assign/edit-assign.component';
import { ActivityComponent } from '../../components/activity/activity.component';
import { CommentDto } from '../../model/comment.type';
import { CommentService } from '../../service/comment.service';
import { DescriptionBoxComponent } from '../../components/description-box/description-box.component';
import { Priorities } from '../../model/priority.type';
import { Authority } from '../../model/role.type';
import { HasAuthorityDirective } from '../../directives/has-autority.directive';
import { EditClientComponent } from './components/edit-client/edit-client.component';
import { SpinnerComponent } from '../../components/spinner/spinner.component';

@Component({
  selector: 'app-ticket-details',
  imports: [FontAwesomeModule, ActivityComponent, DescriptionBoxComponent, EditAssignComponent, EditPriorityComponent, EditClientComponent, SpinnerComponent, HasAuthorityDirective],
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

  owner = signal(false);

  ticket = signal<TicketResponse>({
    id: 0,
    title: '',
    description: '',
    priority: 'LOW',
    status: 'OPEN',
    lock: false,
    createdBy: {
      firstname: '',
      email: ''
    },
    assignedTo: null,
    createdAt: new Date(),
    lastUpdated: new Date(),
    clientName: null,
    clientEmail: null
  });

  ticketId = signal(this.route.snapshot.params['id']);
  
  editTitle = signal(false);
  editAssignee = signal(false);
  editPriority = signal(false);
  editClient = signal(false);
  util = signal(false);

  priority = Priorities;
  status = Status;

  loading = signal(true);
  processingComment = signal(false);
  processingStatus = signal(false);
  processingTitle = signal(false);

  ngOnInit(): void {
    this.loading.set(true);
    this.ticketService.fetchTicket(this.ticketId()).subscribe({
      next: (res) => {
        this.ticket.set(res);
        this.owner.set(this.ticket().createdBy.firstname === this.authService.user.firstname);
        this.loading.set(false);
      }
    });

    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  getColor(username: any): string {
    if (!username) {return '#ddd'}
    return generateColor(username);
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
        status: this.ticket().status == 'OPEN' ? 'CLOSE' : 'OPEN',
        assignedTo: null,
        clientId: null
      }

      this.processingStatus.set(true);
      this.ticketService.updateTicket(this.ticketId(), request).subscribe({
        next: (response) => {
          this.ticket.set(response);
          this.processingStatus.set(false);
          this.activity.ngOnInit(); 
        },
        error: () => {
          this.processingStatus.set(false);
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
    const title = (this.titleField.nativeElement as HTMLInputElement).value;

    if (title === '' || title === this.ticket().title) {return;}
    const request: PatchTicketRequest = {
      title: title,
      description: null,
      priority: null,
      status: null,
      assignedTo: null,
      clientId: null
    }

    this.processingTitle.set(true);
    this.ticketService.updateTicket(this.ticketId(), request).subscribe({
      next: (response) => {
        this.toggleEditTitle();
        this.ticket.set(response);
        this.processingTitle.set(false);
        this.activity.ngOnInit();
      },
      error: () => {
        this.processingTitle.set(false);
      }
    })
  }

  toggleEditTitle() {
    if (this.loading()) {
      return;
    }
    this.editTitle.update(toggle => !toggle);
  }

  toggleEditAssignee() {
    if (this.ticket().lock) {
      return;
    }
    this.editAssignee.update(toggle => !toggle);
  }

  toggleEditClient() {
    if (this.ticket().lock) {
      return;
    }
    this.editClient.update(toggle => !toggle);
  }

  toggleEditPriority() {
    if (this.ticket().lock || !this.isOwnerOrHasAuthority('ticket::edit')) {return;}
    this.editPriority.update(toggle => !toggle);
  }

  toggleUtil() {
    if (this.loading()) {
      return;
    }
    this.util.update(toggle => !toggle);
  }

  delete() {
    this.ticketService.deleteTicket(this.ticketId()).subscribe({
      next: (res) => {
        this.router.navigate(['/tickets'], {replaceUrl: true})
      }
    })
  }

  comment(message: HTMLTextAreaElement) {
    if (message.value !== '') {
      const request: CommentDto = {
        ticketId: this.ticketId(),
        message: message.value
      }

      this.processingComment.set(true);
      this.commentService.createComment(request).subscribe({
        next: () => {
          message.value = '';
          this.processingComment.set(false);
          this.activity.ngOnInit()
        },
        error: () => {
          this.processingComment.set(false);
        }
      })
    }
  }

  hasAuthority(authority: Authority) {
    return this.authService.user.role.authorities.includes(authority);
  }

  isOwnerOrHasAuthority(authority: Authority) {
    return this.owner() || this.hasAuthority(authority);
  }
}
