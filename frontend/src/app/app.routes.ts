import { Routes } from '@angular/router';
import { ClaudeChatComponent } from './pages/claude-chat/claude-chat.component';
import { SettingsComponent } from './pages/settings/settings.component';

export const routes: Routes = [
  { path: '', component: ClaudeChatComponent },
  { path: 'settings', component: SettingsComponent },
  { path: '**', redirectTo: '' }
];
