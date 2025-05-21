import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Alert } from '../../../model/alert.type';
import { ClientRequest, ClientResponse } from '../../../model/client.type';
import { AlertService } from '../../../service/alert.service';
import { ClientService } from '../../../service/client.service';

@Component({
  selector: 'app-edit-client-form',
  imports: [ReactiveFormsModule],
  templateUrl: './edit-client-form.component.html',
  styleUrl: './edit-client-form.component.css'
})
export class EditClientFormComponent implements OnInit {
  client = input.required<ClientResponse>();
  cancel = output<boolean>();

  clientService = inject(ClientService);

  form: FormGroup;
  formErrors = signal(false);

  constructor(private alertService: AlertService) {
    this.form = new FormGroup({
      name: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      phone: new FormControl('')
    })
  }

  ngOnInit(): void {
    this.form.controls['name'].setValue(this.client().name);
    this.form.controls['email'].setValue(this.client().email);
    this.form.controls['phone'].setValue(this.client().phone);
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

    this.clientService.updateClient(this.client().id, request).subscribe({
      next: (res) => {
        const alert: Alert = {
          title: 'Client Updated',
          message: `Client updated with id ${res.id}`,
          type: 'INFO'
        };
        this.alertService.alert = alert;
        this.cancel.emit(true);
      },
      error: (err) => {
        const alert: Alert = {
          title: 'Update Client Fail',
          message: `Fail to update client`,
          type: 'FAIL'
        };
        this.alertService.alert = alert;
      }
    })
  }
}
