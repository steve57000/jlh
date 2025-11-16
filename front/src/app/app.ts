import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClientPortalComponent } from './features/client-portal/client-portal.component';
import { AdminConsoleComponent } from './features/admin-console/admin-console.component';

@Component({
  selector: 'app-root',
  imports: [CommonModule, ClientPortalComponent, AdminConsoleComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {}
