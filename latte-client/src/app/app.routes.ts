import { Routes } from '@angular/router';
import { AuthComponent } from './pages/auth/auth.component';
import { HomeComponent } from './pages/home/home.component';
import { TicketDetailsComponent } from './routes/ticket-details/ticket-details.component';
import { UserComponent } from './routes/user/user.component';
import { TicketComponent } from './routes/ticket/ticket.component';
import { ProfileComponent } from './routes/profile/profile.component';
import { homeGuard } from './guard/home.guard';
import { DashboardComponent } from './routes/dashboard/dashboard.component';

export const routes: Routes = [
  {path: '', pathMatch: 'full', redirectTo: 'home'},
  {path: 'sign-in', component: AuthComponent},
  {
    path: 'home', 
    component: HomeComponent,
    children: [
      {path: 'dashboard', component: DashboardComponent},
      {path: 'tickets', component: TicketComponent},
      {path: 'tickets/:id', component: TicketDetailsComponent},
      {path: 'users', component: UserComponent},
      {path: 'profile', component: ProfileComponent},
      {path: '', pathMatch: 'full', redirectTo: 'dashboard'}
    ],
    canActivate: [homeGuard]
  }
];
