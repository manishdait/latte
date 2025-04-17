import { Injectable } from "@angular/core";
import { Subject } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  private _alert: Subject<string | undefined> = new Subject();

  constructor() {
    this._alert.next(undefined);
  }

  get alert$() {
    return this._alert.asObservable();
  }

  set alert(message: string) {
    this._alert.next(message);
  }
}