# Transport Timetable

A smart transport planner for Zepp devices, with a Kotlin backend and a Kotlin/JS frontend for route lookup, trip planning, and live timetable exploration.

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Zepp%20OS-blue" alt="Zepp OS" />
  <img src="https://img.shields.io/badge/Backend-Ktor-orange" alt="Ktor" />
  <img src="https://img.shields.io/badge/Frontend-Kotlin%2FJS-purple" alt="Kotlin/JS" />
  <img src="https://img.shields.io/badge/Status-Active-green" alt="Status" />
</p>

## Overview

Transport Timetable is a travel companion app designed for quick and convenient route planning on wearable devices. Users can:

- search for origins and destinations
- choose departure date and time
- view available transport routes
- inspect trip details
- save favorite locations
- browse route maps and journey information

The project is split into three main parts:

- `transport-timetable-app` — Zepp smartwatch application
- `transport-timetable-backend` — API server built with Ktor
- `transport-timetable-frontend` — web frontend powered by Kotlin/JS

---

## Features

- Route search between two locations
- Date and time selection for departure planning
- Transport route listing with trip summary
- Favorite place management
- Map-based route visualization
- Lightweight, wearable-friendly UI for Zepp devices
- Backend-powered data flow and route retrieval

---

## Project Structure

```text
transport-timetable/
├── Dockerfile
├── info.json
├── README.md
├── transport-timetable-app/
│   ├── app.js
│   ├── app.json
│   ├── common/
│   ├── page/
│   ├── setting/
│   └── app-side/
├── transport-timetable-backend/
│   ├── src/
│   ├── build.gradle.kts
│   ├── gradlew
│   └── README.md
├── transport-timetable-frontend/
│   ├── src/
│   ├── build.gradle.kts
│   ├── gradlew
│   └── README.md
└── README.md
```

---

## Architecture

### 1. Zepp app
The smartwatch app provides the user-facing interface and interaction flow. It includes pages for:

- home screen
- route selection
- date and time picking
- favorites
- route details
- map view

### 2. Backend
The backend exposes APIs for route-related data and acts as the service layer between the app and transport/route information sources.

### 3. Frontend
The frontend is a web-based interface for the UI and static assets built with Kotlin/JS and Gradle.

---

## Getting Started

### Prerequisites

Before running the project, make sure you have:

- Java 17+
- Gradle
- Node.js / npm
- Zepp OS development tools for the wearable app
- Access to the project’s configured route data backend

---

## Running the App

### Smartwatch app

```bash
cd transport-timetable-app
npm install
npm run simulator:dev
```

You can also use the production build scripts:

```bash
npm run build:prod
npm run preview:prod
```

---

## Running the Backend

```bash
cd transport-timetable-backend
./gradlew run
```

For a full build:

```bash
./gradlew build
```

---

## Running the Frontend

```bash
cd transport-timetable-frontend
./gradlew run
```

Build the web bundle:

```bash
./gradlew jsBrowserDistribution
```

---

## Development Notes

- App metadata and release information are stored in `info.json`
- The Zepp app configuration is defined in `transport-timetable-app/app.json`
- Build scripts and version handling are managed through the app’s helper scripts and Gradle tasks

---

## Data & Attribution

This project uses transit and map data from [Transitous sources](https://transitous.org/sources/). Please review and comply with the attribution and licensing conditions imposed by those sources before publishing, distributing, or otherwise sharing this app or its data.

In particular, because Transitous includes data derived from [OpenStreetMap](https://www.openstreetmap.org/), you must also comply with the OpenStreetMap attribution requirements and terms of use when applicable.

Please keep the required attribution notices visible where appropriate and follow all source-specific conditions in the linked documentation.

---

## License

This project is distributed under the project’s default license terms. Please check the repository for the exact licensing details before distributing or modifying the code.

---

## Contributors

This project was developed by Bitto Kazi and is aimed at delivering a practical travel assistant experience for Zepp wearable users.

---

## Contact

For questions, contribution requests, or collaboration inquiries, use the repository issues or project contact information available in the app metadata.

