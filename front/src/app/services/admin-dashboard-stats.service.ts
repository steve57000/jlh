import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

export interface AdminYearlyStats {
  year: number;
  serviceCount: number;
  serviceRevenue: number;
  devisCount: number;
  devisRevenue: number;
  rendezVousCount: number;
  forecast: boolean;
}

export interface AdminDashboardStats {
  currentYear: number;
  yearly: AdminYearlyStats[];
}

@Injectable({ providedIn: 'root' })
export class AdminDashboardStatsService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/admin/dashboard-stats`;

  getStats() {
    return this.http.get<AdminDashboardStats>(this.base);
  }
}
