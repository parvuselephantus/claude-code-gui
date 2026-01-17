import { Injectable } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import SockJS from 'sockjs-client';
import { ConfigService } from './config.service';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private stompClient: Client | null = null;
  private connectionStatus = new BehaviorSubject<boolean>(false);
  private messageSubjects: Map<string, Subject<any>> = new Map();
  private stompSubscriptions: Map<string, StompSubscription> = new Map();
  private connectionCount = 0;
  private reconnectAttempt = 0;
  private maxReconnectAttempts = 5;
  private disconnectTimerId: any = null;

  public connectionStatus$ = this.connectionStatus.asObservable();

  constructor(private configService: ConfigService) {
    window.addEventListener('focus', () => {
      if (this.connectionCount > 0 && !this.isConnected()) {
        this.connect();
      }
    });
  }

  /**
   * Request a WebSocket connection. Call this in component ngOnInit.
   */
  public requestConnection(): void {
    if (this.disconnectTimerId) {
      clearTimeout(this.disconnectTimerId);
      this.disconnectTimerId = null;
    }

    this.connectionCount++;
    console.log(`WebSocket connection requested. Count: ${this.connectionCount}`);

    if (!this.isConnected()) {
      this.connect();
    }
  }

  /**
   * Release a WebSocket connection. Call this in component ngOnDestroy.
   */
  public releaseConnection(): void {
    this.connectionCount = Math.max(0, this.connectionCount - 1);
    console.log(`WebSocket connection released. Count: ${this.connectionCount}`);

    if (this.connectionCount === 0) {
      if (this.disconnectTimerId) {
        clearTimeout(this.disconnectTimerId);
      }

      this.disconnectTimerId = setTimeout(() => {
        if (this.connectionCount === 0) {
          this.disconnect();
        }
        this.disconnectTimerId = null;
      }, 5000);
    }
  }

  public getMessages<T>(topic: string): Observable<T> {
    if (!this.messageSubjects.has(topic)) {
      const subject = new Subject<T>();
      this.messageSubjects.set(topic, subject);

      const subscribeToTopic = () => {
        if (this.stompClient && this.isConnected() && !this.stompSubscriptions.has(topic)) {
          const stompSubscription = this.stompClient.subscribe(topic, message => {
            try {
              const payload = JSON.parse(message.body);
              subject.next(payload);
            } catch (err) {
              console.error('Error parsing message', err);
            }
          });
          this.stompSubscriptions.set(topic, stompSubscription);
        }
      };

      if (this.isConnected()) {
        subscribeToTopic();
      } else {
        let connectionSub: any;
        connectionSub = this.connectionStatus$.subscribe(connected => {
          if (connected) {
            subscribeToTopic();
            if (connectionSub) {
              connectionSub.unsubscribe();
            }
          }
        });
      }
    }

    return this.messageSubjects.get(topic)!.asObservable();
  }

  public unsubscribe(topic: string): void {
    const stompSub = this.stompSubscriptions.get(topic);
    if (stompSub) {
      stompSub.unsubscribe();
      this.stompSubscriptions.delete(topic);
    }

    const subject = this.messageSubjects.get(topic);
    if (subject) {
      subject.complete();
      this.messageSubjects.delete(topic);
    }
  }

  public isConnected(): boolean {
    return this.stompClient?.connected || false;
  }

  private connect(): void {
    if (this.stompClient && this.isConnected()) {
      console.log('Already connected to WebSocket');
      return;
    }

    console.log('Connecting to WebSocket...');

    const apiUrl = this.configService.getApiUrl();
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(`${apiUrl}/ws`),
      reconnectDelay: this.getReconnectDelay(),
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        // console.log('STOMP Debug: ' + str);
      }
    });

    this.stompClient.onConnect = () => {
      console.log('WebSocket connected');
      this.connectionStatus.next(true);
      this.reconnectAttempt = 0;
    };

    this.stompClient.onStompError = (frame) => {
      console.error('STOMP error', frame);
      this.connectionStatus.next(false);
    };

    this.stompClient.onWebSocketClose = () => {
      console.log('WebSocket closed');
      this.connectionStatus.next(false);
      this.handleReconnect();
    };

    this.stompClient.activate();
  }

  private disconnect(): void {
    console.log('Disconnecting from WebSocket...');

    // Unsubscribe all topics
    this.stompSubscriptions.forEach((sub) => sub.unsubscribe());
    this.stompSubscriptions.clear();

    // Complete all subjects
    this.messageSubjects.forEach((subject) => subject.complete());
    this.messageSubjects.clear();

    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }

    this.connectionStatus.next(false);
    this.reconnectAttempt = 0;
  }

  private handleReconnect(): void {
    if (this.reconnectAttempt < this.maxReconnectAttempts && this.connectionCount > 0) {
      this.reconnectAttempt++;
      console.log(`Reconnection attempt ${this.reconnectAttempt} of ${this.maxReconnectAttempts}`);
      setTimeout(() => this.connect(), this.getReconnectDelay());
    } else if (this.reconnectAttempt >= this.maxReconnectAttempts) {
      console.error('Max reconnection attempts reached');
    }
  }

  private getReconnectDelay(): number {
    const delays = [1000, 2000, 4000, 8000, 16000];
    return delays[Math.min(this.reconnectAttempt, delays.length - 1)];
  }
}
