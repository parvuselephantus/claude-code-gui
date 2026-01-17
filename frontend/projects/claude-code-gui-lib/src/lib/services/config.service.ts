import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

interface AppConfig {
  apiUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  private config: AppConfig = { apiUrl: 'http://localhost:8080' };
  private loaded = false;

  constructor(private http: HttpClient) {}

  async loadConfig(): Promise<void> {
    if (this.loaded) {
      return;
    }

    try {
      this.config = await firstValueFrom(
        this.http.get<AppConfig>('/config.json')
      );
      this.loaded = true;
      console.log('Loaded configuration:', this.config);
    } catch (error) {
      console.warn('Failed to load config.json, using defaults:', error);
      this.loaded = true;
    }
  }

  getApiUrl(): string {
    return this.config.apiUrl;
  }
}
