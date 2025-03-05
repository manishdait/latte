import { Component, EventEmitter, forwardRef, Input, Output } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { CustomControlDirective } from '../../shared/directive/custom-control.directive';

@Component({
  selector: 'app-dropdown',
  imports: [FontAwesomeModule, ReactiveFormsModule],
  templateUrl: './dropdown.component.html',
  styleUrl: './dropdown.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DropdownComponent),
      multi: true,
    },
  ],
})
export class DropdownComponent extends CustomControlDirective {
  @Input('id') id: string | undefined;
  @Input('placeholder') placeholder: string | undefined;
  @Input('list') list: string[] | undefined;
  @Input('hasMore') hasMore: boolean | undefined;
  @Input('hasUnassign') unassign: boolean | undefined;

  @Output('showMore') showMore: EventEmitter<boolean> = new EventEmitter();

  dropdown: boolean = false;

  toggleDropdown() {
    this.dropdown = !this.dropdown;
  }

  select(value: string) {
    this.control!.setValue(value);
    this.dropdown = false;
  }
}
