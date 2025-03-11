import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { Priority } from '../../../../model/priority.enum';
import { TicketResponse, PatchTicketRequest } from '../../../../model/ticket.type';
import { TicketService } from '../../../../service/ticket.service';
import { fontawsomeIcons } from '../../../../shared/fa-icons';

@Component({
  selector: 'app-edit-priority',
  imports: [ReactiveFormsModule, FontAwesomeModule],
  templateUrl: './edit-priority.component.html',
  styleUrl: './edit-priority.component.css'
})
export class EditPriorityComponent {
  @Input('ticket') ticket: TicketResponse | undefined;
  @Output('toggle') toggle: EventEmitter<boolean> = new EventEmitter();
  @Output('changes') changes: EventEmitter<boolean> = new EventEmitter();

  priority: Priority | undefined;

  dropdown: boolean = false;

  list: string[] = ['Low', 'Medium', 'High'];
  priorities: Record<string, Priority> = {'Low': Priority.LOW, 'Medium': Priority.MEDIUM, 'High': Priority.HIGH};

  constructor(private faLibrary: FaIconLibrary, private ticketService: TicketService) {}

  ngOnInit(): void {
    this.priority = this.ticket!.priority;
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  toggleDropdown() {
    this.dropdown = !this.dropdown;
  }

  setPriority(priority: string) {
    this.priority = this.priorities[priority];
    this.toggleDropdown();
  }

  onSubmit() {
    if(this.ticket && this.priority) {
      const request: PatchTicketRequest = {
        title: null,
        description: null,
        priority: this.priority,
        status: null,
        assignedTo: null
      }

      this.ticketService.updateTicket(this.ticket.id, request).subscribe({
        next: (response) => {
          this.changes.emit(true);
          this.cancel();
        }
      })
    }
  }

  getPriority(priority: Priority) {
    if (priority === Priority.LOW) {
      return 'Low';
    } else if (priority === Priority.MEDIUM) {
      return 'Medium';
    } else {
      return 'High';
    }
  }

  cancel() {
    this.toggle.emit(false);
  }
}

