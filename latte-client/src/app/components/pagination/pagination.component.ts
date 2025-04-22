import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  imports: [],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.css'
})
export class PaginationComponent {
  hasPrev = input.required<boolean>();
  hasNext = input.required<boolean>();
  page = input.required<number>();

  onNext = output<boolean>();
  onPrev = output<boolean>();

  getNext() {
    this.onNext.emit(true);
  }

  getPrev() {
    this.onPrev.emit(true);
  }
}
