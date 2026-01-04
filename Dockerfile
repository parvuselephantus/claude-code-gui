FROM node:20-alpine AS frontend-builder

WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS backend-builder

WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn dependency:go-offline
COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install Node.js for frontend dev server
RUN apk add --no-cache nodejs npm

# Copy backend JAR
COPY --from=backend-builder /app/backend/target/claude-code-gui-backend-1.0.0.jar ./backend.jar

# Copy frontend
COPY --from=frontend-builder /app/frontend /app/frontend

# Copy startup scripts
COPY docker/entrypoint.sh ./
RUN chmod +x entrypoint.sh

EXPOSE 4200 8080

ENTRYPOINT ["./entrypoint.sh"]
