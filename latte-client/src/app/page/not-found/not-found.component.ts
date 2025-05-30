import { Component, inject, OnInit } from '@angular/core';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';

@Component({
  selector: 'app-not-found',
  imports: [FontAwesomeModule],
  templateUrl: './not-found.component.html',
  styleUrl: './not-found.component.css'
})
export class NotFoundComponent implements OnInit {
  faLibrary = inject(FaIconLibrary);

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }
}
