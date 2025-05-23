import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { PatchTicketRequest } from '../../../../model/ticket.type';
import { TicketService } from '../../../../service/ticket.service';
import { UserService } from '../../../../service/user.service';
import { fontawsomeIcons } from '../../../../shared/fa-icons';
import { DropdownComponent } from '../../../../components/dropdown/dropdown.component';

@Component({
  selector: 'app-edit-assign',
  imports: [ReactiveFormsModule, DropdownComponent],
  templateUrl: './edit-assign.component.html',
  styleUrl: './edit-assign.component.css'
})
export class EditAssignComponent implements OnInit {
  userService = inject(UserService);
  ticketService = inject(TicketService);
  faLibrary = inject(FaIconLibrary);

  ticketId = input.required<number>();
  value = input.required<string>();
  cancel = output<boolean>();
  changes = output<boolean>();

  dropdown = signal(false);
  engineers = signal<string[]>([]);
  hasNext = signal(false);
  page = signal(0);
  size = signal(5);

  form: FormGroup;

  constructor() {
    this.userService.fetchUserList(this.page(), this.size()).subscribe((data) => {
      this.engineers.set(data.content);
      this.hasNext.set(data.next);
    })

    this.form = new FormGroup({
      assignedTo: new FormControl('')
    })
  }

  ngOnInit(): void {
    this.form.controls['assignedToi'].setValue(this.value())
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  toggleDropdown() {
    this.dropdown.update(toggle => !toggle);
  }

  showMore() {
    this.page.update(count => count + 1);

    this.userService.fetchUserList(this.page(), this.size()).subscribe((data) => {
      this.engineers.update(arr => arr.concat(data.content));
      this.hasNext.set(data.next);
    })
  }

  onSubmit() {
    const request: PatchTicketRequest = {
      title: null,
      description: null,
      priority: null,
      status: null,
      assignedTo: this.form.get('assignedTo')?.value,
      clientId: null
    }

    this.ticketService.updateTicket(this.ticketId(), request).subscribe({
      next: (response) => {
        this.changes.emit(true);
        this.toggleCancel();
      }
    })
  }

  toggleCancel() {
    this.cancel.emit(true);
  }
}

