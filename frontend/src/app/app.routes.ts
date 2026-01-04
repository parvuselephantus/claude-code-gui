import { Routes } from '@angular/router';
import { ClaudeChatComponent } from './pages/claude-chat/claude-chat.component';

export const routes: Routes = [
  { path: '', component: ClaudeChatComponent },
  { path: '**', redirectTo: '' }
];
