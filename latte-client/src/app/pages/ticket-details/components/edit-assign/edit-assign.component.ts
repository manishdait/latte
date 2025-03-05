import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TicketResponse, PatchTicketRequest } from '../../../../model/ticket.type';
import { TicketService } from '../../../../service/ticket.service';
import { UserService } from '../../../../service/user.service';
import { fontawsomeIcons } from '../../../../shared/fa-icons';

@Component({
  selector: 'app-edit-assign',
  imports: [ReactiveFormsModule, FontAwesomeModule],
  templateUrl: './edit-assign.component.html',
  styleUrl: './edit-assign.component.css'
})
export class EditAssignComponent implements OnInit {
  @Input('ticket') ticket: TicketResponse | undefined;
  @Output('toggle') toggle: EventEmitter<boolean> = new EventEmitter();
  @Output('changes') changes: EventEmitter<boolean> = new EventEmitter();

  assignedTo: string = '';

  dropdown: boolean = false;

  engineers: string[] = [];
  hasMore: boolean = false;

  page: number = 0;
  size: number = 5;

  constructor(private faLibrary: FaIconLibrary, private ticketService: TicketService, private userService: UserService) {
    userService.fetchUserList(this.page, this.size).subscribe((data) => {
      this.engineers = this.engineers.concat(data.content);
      this.hasMore = data.next;
    })
  }

  ngOnInit(): void {
    this.assignedTo = this.ticket!.assignedTo.firstname;
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  toggleDropdown() {
    this.dropdown = !this.dropdown;
  }

  setAssignedTo(firstname: string) {
    this.assignedTo = firstname;
    this.toggleDropdown();
  }
  unassigned() {
    this.assignedTo = '';
    this.toggleDropdown();
  }

  showMore() {
    this.page += 1;

    this.userService.fetchUserList(this.page, this.size).subscribe((data) => {
      this.engineers = this.engineers.concat(data.content);
      this.hasMore = data.next;
    })
  }

  onSubmit() {
    const request: PatchTicketRequest = {
      title: null,
      description: null,
      priority: null,
      status: null,
      assignedTo: this.assignedTo
    }

    if(this.ticket) {
      this.ticketService.updateTicket(this.ticket.id, request).subscribe({
        next: (response) => {
          this.changes.emit(true);
          this.cancel();
        }
      })
    }
  }

  cancel() {
    this.toggle.emit(false);
  }
}

