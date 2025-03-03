import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Priority } from '../../models/priority.enum';
import { TicketRequest } from '../../models/ticket.type';
import { Status } from '../../models/status.enum';
import { TicketService } from '../../service/ticket.service';
import { UserService } from '../../service/user.service';
import { UserResponse } from '../../models/user.type';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { Store } from '@ngrx/store';
import { AppState } from '../../state/app.state';
import { addTicket, incrementTicketOpenCount } from '../../state/ticket/ticket.action';
import { AlertService } from '../../service/alert.service';

@Component({
  selector: 'app-create-ticket',
  imports: [ReactiveFormsModule, FontAwesomeModule],
  templateUrl: './create-ticket.component.html',
  styleUrl: './create-ticket.component.css'
})
export class CreateTicketComponent implements OnInit {
  @Output('toggle') toggle: EventEmitter<boolean> = new EventEmitter();

  form: FormGroup;
  priority: string = 'LOW';
  assignedTo: string = '';

  formErrors: boolean = false;
  dropdown: boolean = false;

  engineers: string[] = [];
  hasMore: boolean = false;

  page: number = 0;
  size: number = 5;

  constructor(private faLibrary: FaIconLibrary, private ticketService: TicketService, private userService: UserService, private alertService: AlertService, private store: Store<AppState>) {
    userService.fetchUserList(this.page, this.size).subscribe((data) => {
      this.engineers = this.engineers.concat(data.content);
      this.hasMore = data.next;
    })

    this.form = new FormGroup({
      title: new FormControl('', [Validators.required]),
      description: new FormControl('')
    })
  }

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  get formControls() {
    return this.form.controls;
  }

  toggleDropdown() {
    this.dropdown = !this.dropdown;
  }

  setPriority(priority: string) {
    this.priority = priority;
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
    if (this.form.invalid) {
      this.formErrors = true;
      return;
    }

    this.formErrors = false;
    const request: TicketRequest = {
      title: this.form.get('title')?.value,
      description: this.form.get('description')?.value,
      priority: Priority[this.priority as keyof typeof Priority],
      status: Status.OPEN,
      assignedTo: this.assignedTo
    }
    this.form.reset();

    this.ticketService.createTicket(request).subscribe({
      next: (response) => {
        this.store.dispatch(addTicket({ticket: response}));
        this.store.dispatch(incrementTicketOpenCount());
        this.alertService.alert = `Ticket created`;
        this.cancel();
      },
      error: (err) => {
        this.alertService.alert = err.error.error
      }
    })
  }

  cancel() {
    this.toggle.emit(false);
  }
}
