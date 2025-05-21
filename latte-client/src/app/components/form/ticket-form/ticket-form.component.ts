import { Component, inject, OnInit, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Priority } from '../../../model/priority.type';
import { TicketRequest } from '../../../model/ticket.type';
import { TicketService } from '../../../service/ticket.service';
import { UserService } from '../../../service/user.service';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../../shared/fa-icons';
import { Store } from '@ngrx/store';
import { AppState } from '../../../state/app.state';
import { addTicket, updateOpenCount, updateTicketCount } from '../../../state/ticket/ticket.action';
import { AlertService } from '../../../service/alert.service';
import { DropdownComponent } from '../../dropdown/dropdown.component';
import { SpinnerComponent } from '../../spinner/spinner.component';
import { Alert } from '../../../model/alert.type';
import { HasAuthorityDirective } from '../../../directives/has-autority.directive';
import { ClientService } from '../../../service/client.service';
import { ClientResponse } from '../../../model/client.type';

@Component({
  selector: 'app-ticket-form',
  imports: [ReactiveFormsModule, FontAwesomeModule, DropdownComponent, SpinnerComponent, HasAuthorityDirective],
  templateUrl: './ticket-form.component.html',
  styleUrl: './ticket-form.component.css'
})
export class TicketFormComponent implements OnInit {
  ticketService = inject(TicketService);
  clientService = inject(ClientService);
  userService = inject(UserService);
  alertService = inject(AlertService);
  faLibrary = inject(FaIconLibrary);

  cancel = output<boolean>();

  formErrors = signal(false);
  
  hasMoreEngineers = signal(false);
  engineers = signal<string[]>([]);
  
  clients = signal<ClientResponse[]>([]);
  hasMoreClients = signal(false);
  
  engineerPageCount = signal(0);
  engineerPageSize = signal(5);

  clientPageCount = signal(0);
  clientPageSize = signal(5);
  
  list: string[] = ['Low', 'Medium', 'High'];
  priorities: Record<string, Priority> = {'Low': 'LOW', 'Medium': 'MEDIUM', 'High': 'HIGH'};

  form: FormGroup;

  processing = signal(false);
  
  constructor(private store: Store<AppState>) {
    this.userService.fetchUserList(this.engineerPageCount(), this.engineerPageSize()).subscribe((data) => {
      this.engineers.set(data.content);
      this.hasMoreEngineers.set(data.next);
    });

    this.clientService.fetchClients(this.clientPageCount(), this.clientPageSize()).subscribe((data) => {
      this.clients.set(data.content);
      this.hasMoreClients.set(data.next);
    });

    this.form = new FormGroup({
      title: new FormControl('', [Validators.required]),
      description: new FormControl(''),
      assignedTo: new FormControl(''),
      priority: new FormControl('', [Validators.required]),
      client: new FormControl('')
    });
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

  showMoreEngineers() {
    this.engineerPageCount.update(count => count + 1);

    this.userService.fetchUserList(this.engineerPageCount(), this.engineerPageSize()).subscribe((data) => {
      this.engineers.update(arr => arr.concat(data.content));
      this.hasMoreEngineers.set(data.next);
    });
  }

  showMoreClients() {
    this.clientPageCount.update(count => count + 1);

    this.clientService.fetchClients(this.clientPageCount(), this.clientPageSize()).subscribe((data) => {
      this.clients.update(arr => arr.concat(data.content));
      this.hasMoreClients.set(data.next);
    });
  }

  getClients() {
    return this.clients().map(c => c.name);
  }

  onSubmit() {
    if (this.form.invalid) {
      this.formErrors.set(true);
      return;
    }
    
    this.formErrors.set(false);

    const client = this.clients().filter(c => c.name === this.form.get('client')?.value);

    const request: TicketRequest = {
      title: this.form.get('title')!.value,
      description: this.form.get('description')!.value,
      priority: this.priorities[this.form.get('priority')?.value],
      status: 'OPEN',
      assignedTo: this.form.get('assignedTo')!.value,
      clientId: client.length === 0? 0 : client[0].id
    }

    this.processing.set(true);

    this.ticketService.createTicket(request).subscribe({
      next: (response) => {
        this.store.dispatch(addTicket({ticket: response}));
        this.store.dispatch(updateOpenCount({count: 1}));
        this.store.dispatch(updateTicketCount({count: 1}));

        const alert: Alert = {
          title: 'Ticket Created',
          message: `A new ticket created with id #${response.id}`,
          type: 'INFO'
        }

        this.alertService.alert = alert;
        this.cancel.emit(true);
      },
      error: (err) => {
        this.processing.set(false);
        this.form.reset();

        const alert: Alert = {
          title: 'Fail to Create Ticket',
          message: err.error.error,
          type: 'FAIL'
        }
        this.alertService.alert = alert;
      }
    })
  }
}
