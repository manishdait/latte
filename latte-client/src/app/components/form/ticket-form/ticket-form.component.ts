import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { priorities, Priority } from '../../../model/priority.enum';
import { TicketRequest } from '../../../model/ticket.type';
import { Status } from '../../../model/status.enum';
import { TicketService } from '../../../service/ticket.service';
import { UserService } from '../../../service/user.service';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../../shared/fa-icons';
import { Store } from '@ngrx/store';
import { AppState } from '../../../state/app.state';
import { addTicket, incrementTicketOpenCount } from '../../../state/ticket/ticket.action';
import { AlertService } from '../../../service/alert.service';
import { DropdownComponent } from '../../dropdown/dropdown.component';

@Component({
  selector: 'app-ticket-form',
  imports: [ReactiveFormsModule, FontAwesomeModule, DropdownComponent],
  templateUrl: './ticket-form.component.html',
  styleUrl: './ticket-form.component.css'
})
export class TicketFormComponent implements OnInit {
  @Output('cancel') cancel: EventEmitter<boolean> = new EventEmitter();

  form: FormGroup;
  formErrors: boolean = false;
  
  engineers: string[] = [];
  hasMore: boolean = false;

  page: number = 0;
  size: number = 5;

  priorities: string[] = priorities;

  constructor(private faLibrary: FaIconLibrary, private ticketService: TicketService, private userService: UserService, private alertService: AlertService, private store: Store<AppState>) {
    userService.fetchUserList(this.page, this.size).subscribe((data) => {
      this.engineers = this.engineers.concat(data.content);
      this.hasMore = data.next;
    })

    this.form = new FormGroup({
      title: new FormControl('', [Validators.required]),
      description: new FormControl(''),
      assignedTo: new FormControl(''),
      priority: new FormControl('', [Validators.required])
    })
  }

  get service() {
    return this.userService;
  }

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  get formControls() {
    return this.form.controls;
  }

  showMore() {
    this.page += 1;

    this.userService.fetchUserList(this.page, this.size).subscribe((data) => {
      this.engineers = this.engineers.concat(data.content);
      this.hasMore = data.next;
    })
  }

  onSubmit() {
    console.log(this.form.controls);
    
    if (this.form.invalid) {
      this.formErrors = true;
      return;
    }
    
    this.formErrors = false;
    const request: TicketRequest = {
      title: this.form.get('title')!.value,
      description: this.form.get('description')!.value,
      priority: Priority[this.form.get('priority')!.value as keyof typeof Priority],
      status: Status.OPEN,
      assignedTo: this.form.get('assignedTo')!.value
    }

    this.ticketService.createTicket(request).subscribe({
      next: (response) => {
        this.store.dispatch(addTicket({ticket: response}));
        this.store.dispatch(incrementTicketOpenCount());
        this.alertService.alert = `Ticket created`;
        this.cancel.emit(true);
      },
      error: (err) => {
        this.form.reset();
        this.alertService.alert = err.error.error;
      }
    })
  }
}
