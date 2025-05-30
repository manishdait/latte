import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SplashScreenService {
  private _processing: BehaviorSubject<boolean>  = new BehaviorSubject(false);

  constructor() {}

  set processing(val: boolean) {
    this._processing.next(val);
  }

  get processing() {
    return this._processing.value;
  }

  get processing$() {
    return this._processing.asObservable();
  }
}
