import { Injectable } from "@angular/core";
import { Subject } from "rxjs";
import { Alert } from "../model/alert.type";

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  private _alert: Subject<Alert | undefined> = new Subject();

  constructor() {
    this._alert.next(undefined);
  }

  get alert$() {
    return this._alert.asObservable();
  }

  set alert(alert: Alert) {
    this._alert.next(alert);
  }
}