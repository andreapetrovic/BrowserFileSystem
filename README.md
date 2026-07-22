# Browser File System

A small browser-based file-system prototype. The Spring Boot backend stores file and folder metadata in MongoDB; the React frontend provides folder navigation, creation, deletion, exact-name search, and file autocomplete.

## Architecture

- `backend/` — Spring Boot 3 / Java 17 REST API and MongoDB persistence
- `frontend/` — React 18 client
- MongoDB — stores file and folder metadata (files have names only; no file contents are stored)

## Prerequisites

- JDK 17
- Node.js 18+
- MongoDB 6+

Start MongoDB locally on `mongodb://localhost:27017`, or change the URI in `backend/src/main/resources/application.properties`.

## Run locally

Start the backend:

```bash
cd backend
./mvnw spring-boot:run
```

It runs on `http://localhost:8080`.

In another terminal, start the frontend:

```bash
cd frontend
npm install
npm start
```

It runs on `http://localhost:3000` and calls the backend at `http://localhost:8080/api`.

## Configuration

| Setting | Default | Purpose |
| --- | --- | --- |
| `REACT_APP_API_URL` | `http://localhost:8080/api` | Frontend API base URL |
| `spring.data.mongodb.uri` | `mongodb://localhost:27017/browser_file_system` | MongoDB connection URI |
| `server.port` | `8080` | Backend HTTP port |

## Development and debug

- Run backend tests: `cd backend && mvn test`
- Run backend tests: `cd backend && ./mvnw test`
- Build the frontend: `cd frontend && npm run build`
- Debug the backend from your IDE by debugging `BrowserFileSystemApplication` with the `dev` profile or with the normal Spring Boot configuration.

## Search behavior

- Typing an exact name searches all items from the root, or only the open folder when inside a folder.
- Autocomplete calls the backend after a short debounce and returns up to ten files whose names start with the typed prefix.
