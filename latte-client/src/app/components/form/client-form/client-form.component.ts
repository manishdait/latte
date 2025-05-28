import { Component, inject, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AlertService } from '../../../service/alert.service';
import { Store } from '@ngrx/store';
import { AppState } from '../../../state/app.state';
import { ClientService } from '../../../service/client.service';
import { ClientRequest } from '../../../model/client.type';
import { addClient } from '../../../state/client/client.action';
import { Alert } from '../../../model/alert.type';
import { SpinnerComponent } from '../../spinner/spinner.component';

@Component({
  selector: 'app-client-form',
  imports: [ReactiveFormsModule, SpinnerComponent],
  templateUrl: './client-form.component.html',
  styleUrl: './client-form.component.css'
})
export class ClientFormComponent {
  cancel = output<boolean>();

  clientService = inject(ClientService);

  form: FormGroup;
  formErrors = signal(false);
  
  processing = signal(false);

  constructor(private alertService: AlertService, private store: Store<AppState>) {
    this.form = new FormGroup({
      name: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      phone: new FormControl('')
    })
  }

  get formControls() {
    return this.form.controls;
  }

  onSubmit() {
    if (this.form.invalid) {
      this.formErrors.set(true);
      return;
    }

    this.formErrors.set(false);

    const request: ClientRequest = {
      name: this.form.get('name')?.value,
      email: this.form.get('email')?.value,
      phone: this.form.get('phone')?.value
    }

    this.processing.set(true);
    this.form.disable();

    this.clientService.createClient(request).subscribe({
      next: (res) => {
        this.store.dispatch(addClient({client: res}));
        const alert: Alert = {
          title: 'Client Created',
          message: `Client created with id ${res.id}`,
          type: 'INFO'
        };
        this.alertService.alert = alert;
        this.cancel.emit(true);
      },
      error: (err) => {
        this.processing.set(false);
        this.form.enable();
        const alert: Alert = {
          title: 'Create Client Fail',
          message: `Fail to create new client`,
          type: 'FAIL'
        };
        this.alertService.alert = alert;
      }
    })
  }
}
