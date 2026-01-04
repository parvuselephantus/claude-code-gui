import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subscription } from 'rxjs';

import { ClaudeService, ClaudeAnalysisRequest, ClaudeProgress } from '../../services/claude.service';
import { WebsocketService } from '../../services/websocket.service';

interface ConversationMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

@Component({
  selector: 'app-claude-chat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSlideToggleModule,
    MatIconModule,
    MatTooltipModule
  ],
  templateUrl: './claude-chat.component.html',
  styleUrls: ['./claude-chat.component.scss']
})
export class ClaudeChatComponent implements OnInit, OnDestroy {
  useMcpMode: boolean = false;
  mcpModeAvailable = false;

  get mode(): 'simple' | 'mcp' {
    return this.useMcpMode ? 'mcp' : 'simple';
  }

  get reversedConversationHistory(): ConversationMessage[] {
    return [...this.conversationHistory].reverse();
  }

  prompt: string = '';
  isAnalyzing: boolean = false;
  currentAnalysisId: string | null = null;
  conversationId: string | null = null;
  result: string = '';
  error: string = '';
  durationMs: number = 0;

  // Conversation history
  conversationHistory: ConversationMessage[] = [];

  claudeAvailable: boolean = false;
  statusChecked: boolean = false;

  // Subscription management
  private completionSubscription: Subscription | null = null;
  private errorSubscription: Subscription | null = null;

  constructor(
    private claudeService: ClaudeService,
    private websocket: WebsocketService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.websocket.requestConnection();
    this.checkStatus();
    this.loadSettings();
  }

  loadSettings(): void {
    const savedMode = localStorage.getItem('useMcpMode');
    if (savedMode !== null) {
      this.useMcpMode = savedMode === 'true';
    } else {
      this.useMcpMode = true;
    }
  }

  goToSettings(): void {
    this.router.navigate(['/settings']);
  }

  ngOnDestroy(): void {
    this.unsubscribeFromTopics();
    this.websocket.releaseConnection();
  }

  checkStatus(): void {
    this.claudeService.getStatus().subscribe({
      next: (status) => {
        this.claudeAvailable = status.claudeAvailable;
        this.mcpModeAvailable = status.mcpMode;
        this.statusChecked = true;

        if (!this.mcpModeAvailable && this.useMcpMode) {
          this.useMcpMode = false;
        }
      },
      error: (err) => {
        console.error('Error checking Claude status:', err);
        this.statusChecked = true;
        this.claudeAvailable = false;
      }
    });
  }

  analyze(): void {
    if (!this.prompt.trim()) {
      this.error = 'Please enter a prompt';
      return;
    }

    if (!this.claudeAvailable) {
      this.error = 'Claude Code is not available';
      return;
    }

    // Add user message to conversation history
    this.conversationHistory.push({
      role: 'user',
      content: this.prompt,
      timestamp: new Date()
    });

    this.isAnalyzing = true;
    this.result = '';
    this.error = '';
    this.durationMs = 0;

    const request: ClaudeAnalysisRequest = {
      prompt: this.prompt,
      mode: this.mode,
      conversationId: this.mode === 'mcp' ? (this.conversationId ?? undefined) : undefined
    };

    const startTime = Date.now();

    if (this.mode === 'simple') {
      this.analyzeSimpleMode(request, startTime);
    } else {
      this.analyzeMcpMode(request, startTime);
    }

    // Clear prompt after sending
    this.prompt = '';
  }

  private analyzeSimpleMode(request: ClaudeAnalysisRequest, startTime: number): void {
    this.claudeService.analyzeSimple(request).subscribe({
      next: (response) => {
        this.currentAnalysisId = response.analysisId;

        // Subscribe to topics for this analysis
        this.subscribeToTopics(this.currentAnalysisId, startTime);
      },
      error: (err) => {
        console.error('Error starting analysis:', err);
        this.error = 'Failed to start analysis: ' + err.message;
        this.isAnalyzing = false;
      }
    });
  }

  private analyzeMcpMode(request: ClaudeAnalysisRequest, startTime: number): void {
    this.claudeService.analyzeMcp(request).subscribe({
      next: (response) => {
        const isNewConversation = !this.conversationId;
        this.currentAnalysisId = response.analysisId;

        if (isNewConversation) {
          // First message in conversation - set up subscriptions
          this.conversationId = response.analysisId;
          this.subscribeToTopics(this.conversationId, startTime);
        } else {
          // Subsequent message - subscriptions already exist, just update state
          // Store start time for this specific analysis
          (this as any)._currentStartTime = startTime;
        }
      },
      error: (err) => {
        console.error('Error starting MCP analysis:', err);
        this.error = 'Failed to start MCP analysis: ' + err.message;
        this.isAnalyzing = false;
      }
    });
  }

  private subscribeToTopics(analysisId: string, startTime: number): void {
    // Store start time for completion handler
    (this as any)._currentStartTime = startTime;

    // Unsubscribe from previous topics if any
    this.unsubscribeFromTopics();

    // Subscribe to completion
    this.completionSubscription = this.claudeService.getCompletion(analysisId).subscribe({
      next: (completion) => {
        this.result = completion.result || '';
        this.durationMs = completion.durationMs || (Date.now() - (this as any)._currentStartTime);
        this.isAnalyzing = false;

        if (this.result) {
          this.conversationHistory.push({
            role: 'assistant',
            content: this.result,
            timestamp: new Date()
          });
        }

        // In simple mode, unsubscribe after completion
        if (this.mode === 'simple') {
          this.unsubscribeFromTopics();
        }
      }
    });

    // Subscribe to errors
    this.errorSubscription = this.claudeService.getError(analysisId).subscribe({
      next: (errorData) => {
        this.error = errorData.error;
        this.isAnalyzing = false;

        // In simple mode, unsubscribe after error
        if (this.mode === 'simple') {
          this.unsubscribeFromTopics();
        }
      }
    });
  }

  private unsubscribeFromTopics(): void {
    if (this.completionSubscription) {
      this.completionSubscription.unsubscribe();
      this.completionSubscription = null;
    }

    if (this.errorSubscription) {
      this.errorSubscription.unsubscribe();
      this.errorSubscription = null;
    }

    // Clean up WebSocket subscriptions
    if (this.conversationId && this.mode === 'mcp') {
      this.claudeService.unsubscribe(this.conversationId);
    } else if (this.currentAnalysisId && this.mode === 'simple') {
      this.claudeService.unsubscribe(this.currentAnalysisId);
    }
  }

  clearResults(): void {
    this.result = '';
    this.error = '';
    this.durationMs = 0;
    this.conversationHistory = [];

    if (this.mode === 'mcp') {
      // Unsubscribe from current conversation topics
      this.unsubscribeFromTopics();
      this.conversationId = null;
    }
  }

  clearPrompt(): void {
    this.prompt = '';
  }

  onModeChange(): void {
    this.error = '';
    localStorage.setItem('useMcpMode', this.useMcpMode.toString());
    this.unsubscribeFromTopics();
    this.conversationId = null;
  }
}
