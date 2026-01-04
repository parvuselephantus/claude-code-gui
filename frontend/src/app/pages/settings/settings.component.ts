import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSelectModule } from '@angular/material/select';
import { ConfigService } from '../../services/config.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSlideToggleModule,
    MatIconModule,
    MatDividerModule,
    MatSelectModule
  ],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  // Mode settings
  useMcpMode: boolean = false;
  mcpModeAvailable: boolean = false;

  constructor(
    private router: Router,
    private http: HttpClient,
    private configService: ConfigService
  ) {}

  ngOnInit(): void {
    const savedMode = localStorage.getItem('useMcpMode');
    if (savedMode !== null) {
      this.useMcpMode = savedMode === 'true';
    } else {
      this.useMcpMode = true;
    }

    this.mcpModeAvailable = true;
  }

  saveSettings(): void {
    localStorage.setItem('useMcpMode', this.useMcpMode.toString());
    this.router.navigate(['/']);
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
