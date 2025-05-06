import { Component, inject, OnInit, signal } from '@angular/core';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-menubar',
  imports: [FontAwesomeModule, RouterLinkActive, RouterLink],
  templateUrl: './menubar.component.html',
  styleUrl: './menubar.component.css'
})
export class MenubarComponent implements OnInit {
  faLibrary = inject(FaIconLibrary);

  sidenav = signal(false);

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  toggleSidenav() {
    this.sidenav.update(toggle => !toggle);
  }
}
