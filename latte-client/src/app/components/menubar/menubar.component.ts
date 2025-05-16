import { Component, inject, OnInit, output, signal } from '@angular/core';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { HasAuthorityDirective } from '../../directives/has-autority.directive';
import { AuthService } from '../../service/auth.service';
import { Store } from '@ngrx/store';
import { AppState } from '../../state/app.state';
import { Observable } from 'rxjs';
import { recentNotification } from '../../state/notification/notification.selector';
import { CommonModule } from '@angular/common';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-menubar',
  imports: [CommonModule, FontAwesomeModule, RouterLinkActive, RouterLink, HasAuthorityDirective],
  templateUrl: './menubar.component.html',
  styleUrl: './menubar.component.css'
})
export class MenubarComponent implements OnInit {
  createTicket = output<boolean>();

  authService = inject(AuthService);
  router = inject(Router);
  faLibrary = inject(FaIconLibrary);

  sidenav = signal(false);

  version = signal(environment.VERSION);
  hasRecentNotification$: Observable<boolean>;

  constructor(private store: Store<AppState>) {
    this.hasRecentNotification$ = store.select(recentNotification);
  }

  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
  }

  toggleSidenav() {
    this.sidenav.update(toggle => !toggle);
  }

  toggleCreateTicket() {
    this.createTicket.emit(true);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/sign-in'], {replaceUrl: true});
  }
}
