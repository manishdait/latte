import { Directive, Inject, Injector, OnInit } from '@angular/core';
import { ControlValueAccessor, FormControl, FormControlDirective, FormControlName, FormGroupDirective, NgControl } from '@angular/forms';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { Subject, takeUntil, startWith, distinctUntilChanged, tap } from 'rxjs';
import { fontawsomeIcons } from '../fa-icons';

@Directive({
  selector: '[appCustomControl]'
})
export class CustomControlDirective implements ControlValueAccessor, OnInit {
  control: FormControl | undefined;
  private _destroy$: Subject<void> = new Subject<void>();
  private disabled: boolean = false;
  private onTouch!: () => any;
  
  constructor(@Inject(Injector) private injector: Injector, private faLibrary: FaIconLibrary) {}
  
  ngOnInit(): void {
    this.setFormControl();
    this.faLibrary.addIcons(...fontawsomeIcons);
  }
  
  setFormControl() {
    try {
      const formControl = this.injector.get(NgControl);
      switch (formControl.constructor) {
        case FormControlName:
          this.control = this.injector.get(FormGroupDirective).getControl(formControl as FormControlName);
          break;
        default:
          this.control = (formControl as FormControlDirective).form as FormControl;
      }
    } catch (err) {
      this.control = new FormControl();
    }
  }
  
  writeValue(obj: any): void {
    if(this.control) {
      if (this.control.value !== obj) {
        this.control.setValue(obj);
      }
    } else {
      this.control = new FormControl(obj);
    }
  }
  
  registerOnChange(fn: any): void {
    this.control!.valueChanges.pipe(
      takeUntil(this._destroy$),
      startWith(this.control!.value),
      distinctUntilChanged(),
      tap(val => fn(val))
    ).subscribe(() => this.control?.markAsUntouched());
  }
  
  registerOnTouched(fn: any): void {
    this.onTouch = fn;
  }
  
  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }
}
