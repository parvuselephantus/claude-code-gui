# Claude Code GUI - Frontend

Angular 20 frontend application for the Claude Code GUI.

## Development

```bash
npm install
npm start
```

Open http://localhost:4200 in your browser.

## Build

```bash
npm run build
```

Build artifacts will be in `dist/frontend`.

## Components

- **ClaudeChatComponent** - Main chat interface
- **ClaudeService** - HTTP and WebSocket client for backend API
- **WebsocketService** - STOMP over SockJS connection management

## Configuration

Backend API URL is configured in `src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
```
