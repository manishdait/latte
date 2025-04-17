import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  imports: [],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.css'
})
export class PaginationComponent {
  prevPage = input.required<boolean>();
  nextPage = input.required<boolean>();
  pageCount = input.required<number>();

  next = output<boolean>();
  prev = output<boolean>();

  getNext() {
    this.next.emit(true);
  }

  getPrev() {
    this.prev.emit(true);
  }
}
