import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';

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
    MatDividerModule
  ],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  // Mode settings
  useMcpMode: boolean = false;
  mcpModeAvailable: boolean = false;

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Load settings from localStorage
    const savedMode = localStorage.getItem('useMcpMode');
    if (savedMode !== null) {
      this.useMcpMode = savedMode === 'true';
    }

    // Check MCP availability (you could call the backend here)
    this.mcpModeAvailable = true; // For now, assume available
  }

  saveSettings(): void {
    // Save to localStorage
    localStorage.setItem('useMcpMode', this.useMcpMode.toString());

    // Navigate back to main page
    this.router.navigate(['/']);
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
