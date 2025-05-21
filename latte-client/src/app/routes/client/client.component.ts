import { Component, inject, OnInit, signal } from '@angular/core';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { HasAuthorityDirective } from '../../directives/has-autority.directive';
import { Store } from '@ngrx/store';
import { AppState } from '../../state/app.state';
import { count, Observable } from 'rxjs';
import { ClientResponse } from '../../model/client.type';
import { ClientService } from '../../service/client.service';
import { client } from '../../state/client/client.selector';
import { removeClient, setClients } from '../../state/client/client.action';
import { ShimmerComponent } from '../../components/shimmer/shimmer.component';
import { CommonModule } from '@angular/common';
import { PaginationComponent } from '../../components/pagination/pagination.component';
import { ClientFormComponent } from '../../components/form/client-form/client-form.component';
import { EditClientFormComponent } from '../../components/form/edit-client-form/edit-client-form.component';
import { DialogComponent } from '../../components/dialog/dialog.component';
import { Alert } from '../../model/alert.type';
import { AlertService } from '../../service/alert.service';

@Component({
  selector: 'app-client',
  imports: [CommonModule, FontAwesomeModule, ClientFormComponent, EditClientFormComponent, PaginationComponent, ShimmerComponent, DialogComponent, HasAuthorityDirective],
  templateUrl: './client.component.html',
  styleUrl: './client.component.css'
})
export class ClientComponent implements OnInit {
  clientService = inject(ClientService);
  alertService = inject(AlertService);
  faLibrary = inject(FaIconLibrary);

  page = signal(0);
  size = signal(10);

  loading = signal(false);

  clients$: Observable<ClientResponse[]>;

  createClient = signal(false);
  editClient = signal(false);
  deleteClient = signal(false);

  clientPage = signal({
    next: false,
    prev: false
  });

  bufferClient = signal<ClientResponse>({
    id: 0,
    name: '',
    email: '',
    phone: '',
    deletable: false
  });

  constructor(private store: Store<AppState>) {
    this.clients$ = store.select(client);
  }
  
  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
    this.getClients();
  }

  getNext() {
    this.page.update(count => count+1);
    this.getClients();
  }

  getPrev() {
    this.page.update(count => count-1);
    this.getClients();
  }

  toggleCreateClinet() {
    this.createClient.update(toggle => !toggle);
  }

  toggleEditClient(client: ClientResponse) {
    this.bufferClient.set(client);
    this.editClient.set(true);
  }

  toggleDeleteClinet(client: ClientResponse) {
    this.bufferClient.set(client);
    this.deleteClient.set(true);
  }

  confirmTrigger(event: boolean) {
    this.deleteClient.set(false);

    if (event) {
      this.clientService.deleteClient(this.bufferClient().id).subscribe({
        next: () => {
          this.store.dispatch(removeClient({id: this.bufferClient().id}));
          const alert: Alert = {
            title: 'Delete Client',
            message: `Client with name ${this.bufferClient().name} deleted`,
            type: 'INFO'
          }
          this.alertService.alert = alert;
        }
      })
    }
  }

  getClients() {
    this.loading.set(true);
    this.clientService.fetchClients(this.page(), this.size()).subscribe({
      next: (res) => {
        this.store.dispatch(setClients({clients: res.content}));
        this.clientPage.update(page => {
          page.next = res.next;
          page.prev = res.prev;
          return page;
        })
        this.loading.set(false);
      }
    })
  }
}
