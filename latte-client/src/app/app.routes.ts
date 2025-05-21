import { Routes } from '@angular/router';
import { homeGuard } from './guard/home.guard';

export const routes: Routes = [
  {
    path: 'sign-in', 
    loadComponent: () => import('./page/auth/auth.component').then(c => c.AuthComponent)
  },
  {
    path: '', 
    loadComponent: () => import('./page/home/home.component').then(c => c.HomeComponent),
    children: [
      {
        path: 'dashboard', 
        loadComponent: () => import('./routes/dashboard/dashboard.component').then(c => c.DashboardComponent)
      },
      {
        path: 'tickets', 
        loadComponent: () => import('./routes/ticket/ticket.component').then(c => c.TicketComponent)
      },
      {
        path: 'tickets/:id', 
        loadComponent: () => import('./page/ticket-details/ticket-details.component').then(c => c.TicketDetailsComponent)
      },
      {
        path: 'users', 
        loadComponent: () => import('./routes/user/user.component').then(c => c.UserComponent)
      },
      {
        path: 'roles', 
        loadComponent: () => import('./routes/role/role.component').then(c => c.RoleComponent)
      },
      {
        path: 'clients',
        loadComponent: () => import('./routes/client/client.component').then(c => c.ClientComponent)
      },
      {
        path: 'profile', 
        loadComponent: () => import('./routes/profile/profile.component').then(c => c.ProfileComponent)
      },
      {
        path: 'notifications', 
        loadComponent: () => import('./page/notification/notification.component').then(c => c.NotificationComponent)
      },
      {
        path: '', 
        pathMatch: 'full', 
        redirectTo: 'dashboard'
      }
    ],
    canActivate: [homeGuard]
  }
];
