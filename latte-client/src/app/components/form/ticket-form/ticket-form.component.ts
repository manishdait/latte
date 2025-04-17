import { Component, inject, OnInit, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Priority } from '../../../model/priority.enum';
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
import { AuthService } from '../../../service/auth.service';

@Component({
  selector: 'app-ticket-form',
  imports: [ReactiveFormsModule, FontAwesomeModule, DropdownComponent],
  templateUrl: './ticket-form.component.html',
  styleUrl: './ticket-form.component.css'
})
export class TicketFormComponent implements OnInit {
  ticketService = inject(TicketService);
  authService = inject(AuthService);
  userService = inject(UserService);
  alertService = inject(AlertService);
  faLibrary = inject(FaIconLibrary);

  cancel = output<boolean>();

  formErrors = signal(false);
  more = signal(false);
  engineers = signal<string[]>([]);
  
  pageCount = signal(0);
  size = signal(5);
  
  list: string[] = ['Low', 'Medium', 'High'];
  priorities: Record<string, Priority> = {'Low': Priority.LOW, 'Medium': Priority.MEDIUM, 'High': Priority.HIGH};

  form: FormGroup;
  
  constructor(private store: Store<AppState>) {
    this.userService.fetchUserList(this.pageCount(), this.size()).subscribe((data) => {
      this.engineers.set(data.content);
      this.more.set(data.next);
    })

    this.form = new FormGroup({
      title: new FormControl('', [Validators.required]),
      description: new FormControl(''),
      assignedTo: new FormControl(''),
      priority: new FormControl('', [Validators.required]),
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
    this.pageCount.update(count => count + 1);

    this.userService.fetchUserList(this.pageCount(), this.size()).subscribe((data) => {
      this.engineers.update(arr => arr.concat(data.content));
      this.more.set(data.next);
    })
  }

  onSubmit() {
    console.log(this.form.controls);
    
    if (this.form.invalid) {
      this.formErrors.set(true);
      return;
    }
    
    this.formErrors.set(false);

    const request: TicketRequest = {
      title: this.form.get('title')!.value,
      description: this.form.get('description')!.value,
      priority: this.priorities[this.form.get('priority')?.value],
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

  assignOps() {
    return this.authService.assignTicket();
  }
}
