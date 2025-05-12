import { Routes } from '@angular/router';
import { AuthComponent } from './page/auth/auth.component';
import { HomeComponent } from './page/home/home.component';
import { TicketDetailsComponent } from './page/ticket-details/ticket-details.component';
import { UserComponent } from './routes/user/user.component';
import { TicketComponent } from './routes/ticket/ticket.component';
import { ProfileComponent } from './routes/profile/profile.component';
import { homeGuard } from './guard/home.guard';
import { DashboardComponent } from './routes/dashboard/dashboard.component';
import { RoleComponent } from './routes/role/role.component';

export const routes: Routes = [
  {path: 'sign-in', component: AuthComponent},
  {
    path: '', 
    component: HomeComponent,
    children: [
      {path: 'dashboard', component: DashboardComponent},
      {path: 'tickets', component: TicketComponent},
      {path: 'tickets/:id', component: TicketDetailsComponent},
      {path: 'users', component: UserComponent},
      {path: 'roles', component: RoleComponent},
      {path: 'profile', component: ProfileComponent},
      {path: '', pathMatch: 'full', redirectTo: 'dashboard'}
    ],
    canActivate: [homeGuard]
  }
];
