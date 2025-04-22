import { Component, inject, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { Priority } from '../../../../model/priority.type';
import { PatchTicketRequest } from '../../../../model/ticket.type';
import { TicketService } from '../../../../service/ticket.service';
import { fontawsomeIcons } from '../../../../shared/fa-icons';
import { DropdownComponent } from '../../../../components/dropdown/dropdown.component';

@Component({
  selector: 'app-edit-priority',
  imports: [DropdownComponent, ReactiveFormsModule, FontAwesomeModule],
  templateUrl: './edit-priority.component.html',
  styleUrl: './edit-priority.component.css'
})
export class EditPriorityComponent {
  ticketId = input.required<number>();
  value = input.required<string>();
  cancel = output<boolean>();
  changes = output<boolean>();

  faLibrary = inject(FaIconLibrary);
  ticketService = inject(TicketService);
  
  list: string[] = ['Low', 'Medium', 'High'];
  priorities: Record<string, Priority> = {'Low': 'LOW', 'Medium': 'MEDIUM', 'High': 'HIGH'};

  form: FormGroup;

  constructor() {
    this.form = new FormGroup({
      priority: new FormControl('')
    });
  }

  ngOnInit(): void {
    this.form.controls['priority'].setValue(this.value());
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  onSubmit() {
    const request: PatchTicketRequest = {
      title: null,
      description: null,
      priority: this.priorities[this.form.get('priority')?.value],
      status: null,
      assignedTo: null
    }

    this.ticketService.updateTicket(this.ticketId(), request).subscribe({
      next: (res) => {
        this.changes.emit(true);
        this.toggleCancel();
      }
    })
  }

  toggleCancel() {
    this.cancel.emit(false);
  }
}

