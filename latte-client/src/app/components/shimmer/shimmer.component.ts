import { Component, input } from '@angular/core';

@Component({
  selector: 'app-shimmer',
  imports: [],
  templateUrl: './shimmer.component.html',
  styleUrl: './shimmer.component.css'
})
export class ShimmerComponent {
  type = input.required<Shimmer>();
  times = input(1);

  getSequence() {
    return Array.from({length: this.times()});
  }
}

export type Shimmer = 'CARD_VIEW' | 'USER_LIST' | 'ROLE_LIST';
