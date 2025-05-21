import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { TicketService } from '../../../../service/ticket.service';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { ClientService } from '../../../../service/client.service';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { DropdownComponent } from '../../../../components/dropdown/dropdown.component';
import { client } from 'stompjs';
import { PatchTicketRequest } from '../../../../model/ticket.type';
import { ClientResponse } from '../../../../model/client.type';

@Component({
  selector: 'app-edit-client',
  imports: [ReactiveFormsModule, DropdownComponent],
  templateUrl: './edit-client.component.html',
  styleUrl: './edit-client.component.css'
})
export class EditClientComponent implements OnInit {
  clientService = inject(ClientService);
  ticketService = inject(TicketService);
  faLibrary = inject(FaIconLibrary);

  ticketId = input.required<number>();
  value = input.required<string>();
  cancel = output<boolean>();
  changes = output<boolean>();

  dropdown = signal(false);
  clients = signal<ClientResponse[]>([]);
  hasNext = signal(false);
  page = signal(0);
  size = signal(5);

  form: FormGroup;

  constructor() {
    this.clientService.fetchClients(this.page(), this.size()).subscribe({
      next: (res) => {
        this.clients.set(res.content);
        this.hasNext.set(res.next);
      }
    })
    this.form = new FormGroup({
      client: new FormControl('')
    })
  }

  
  ngOnInit(): void {
    this.form.controls['client'].setValue(this.value());
  }

  toggleDropdown() {
    this.dropdown.update(toggle => !toggle);
  }

  showMore() {
    this.page.update(count => count + 1);

    this.clientService.fetchClients(this.page(), this.size()).subscribe({
      next: (res) => {
        this.clients.update(arr => arr.concat(res.content));
        this.hasNext.set(res.next);
      }
    })
  }

  getClients() {
    return this.clients().map(c => c.name);
  }

  onSubmit() {
    const client = this.clients().filter(c => c.name === this.form.get('client')?.value);
    
    const request: PatchTicketRequest = {
      title: null,
      description: null,
      priority: null,
      status: null,
      assignedTo: null,
      clientId: client.length === 0? 0 : client[0].id
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
