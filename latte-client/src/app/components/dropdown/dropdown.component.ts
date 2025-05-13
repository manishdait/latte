import { Component, forwardRef, input, output, signal } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { CustomControlDirective } from '../../directives/custom-control.directive';

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
  id = input('');
  placeholder = input('');
  dropdownItems = input<string[]>();
  hasMore = input(false);
  hasUnassign = input(false);

  onNext = output<boolean>();

  dropdown = signal(false);

  toggleDropdown() {
    this.dropdown.update(toggle => !toggle);
  }

  select(value: string) {
    this.control!.setValue(value);
    this.dropdown.set(false);
  }
}
