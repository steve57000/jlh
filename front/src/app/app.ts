import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ClientPortalComponent } from './features/client-portal/client-portal.component';
import { AdminConsoleComponent } from './features/admin-console/admin-console.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ClientPortalComponent, AdminConsoleComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {}
