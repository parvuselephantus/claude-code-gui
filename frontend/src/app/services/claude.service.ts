import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConfigService } from './config.service';
import { WebsocketService } from './websocket.service';

export interface ClaudeAnalysisRequest {
  prompt: string;
  mode: 'simple' | 'mcp';
  conversationId?: string;
}

export interface ClaudeAnalysisResponse {
  analysisId: string;
  result?: string;
  mode: string;
  completed: boolean;
  error?: string;
  conversationId?: string;
  durationMs?: number;
}

export interface ClaudeProgress {
  message: string;
  timestamp: number;
}

export interface ClaudeStatus {
  claudeAvailable: boolean;
  simpleMode: boolean;
  mcpMode: boolean;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ClaudeService {
  private get baseUrl(): string {
    return this.configService.getApiUrl() + '/api/claude';
  }

  constructor(
    private http: HttpClient,
    private websocket: WebsocketService,
    private configService: ConfigService
  ) {}

  /**
   * Get Claude Code status
   */
  getStatus(): Observable<ClaudeStatus> {
    return this.http.get<ClaudeStatus>(`${this.baseUrl}/status`);
  }

  /**
   * Start analysis in simple mode (no conversation)
   */
  analyzeSimple(request: ClaudeAnalysisRequest): Observable<ClaudeAnalysisResponse> {
    request.mode = 'simple';
    return this.http.post<ClaudeAnalysisResponse>(`${this.baseUrl}/analyze-simple`, request);
  }

  /**
   * Start analysis in MCP mode (conversation support)
   */
  analyzeMcp(request: ClaudeAnalysisRequest): Observable<ClaudeAnalysisResponse> {
    request.mode = 'mcp';
    return this.http.post<ClaudeAnalysisResponse>(`${this.baseUrl}/analyze-mcp`, request);
  }

  /**
   * Subscribe to progress updates for an analysis
   */
  getProgressUpdates(analysisId: string): Observable<ClaudeProgress> {
    return this.websocket.getMessages<ClaudeProgress>(
      `/topic/claude_analysis_progress/${analysisId}`
    );
  }

  /**
   * Subscribe to completion notification for an analysis
   */
  getCompletion(analysisId: string): Observable<ClaudeAnalysisResponse> {
    return this.websocket.getMessages<ClaudeAnalysisResponse>(
      `/topic/claude_analysis_complete/${analysisId}`
    );
  }

  /**
   * Subscribe to error notifications for an analysis
   */
  getError(analysisId: string): Observable<{error: string}> {
    return this.websocket.getMessages<{error: string}>(
      `/topic/claude_analysis_error/${analysisId}`
    );
  }

  /**
   * Unsubscribe from analysis updates
   */
  unsubscribe(analysisId: string): void {
    this.websocket.unsubscribe(`/topic/claude_analysis_progress/${analysisId}`);
    this.websocket.unsubscribe(`/topic/claude_analysis_complete/${analysisId}`);
    this.websocket.unsubscribe(`/topic/claude_analysis_error/${analysisId}`);
  }
}
