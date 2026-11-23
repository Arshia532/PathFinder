# PathFinder Visualizer — Frontend

React + TypeScript client for PathFinder Visualizer. The Java Spring Boot
backend (`../backend`) runs every search and returns the recorded steps;
this app draws the grid and replays those steps.

## Tech stack

- React 19 + TypeScript, built with Vite
- CSS Modules with design tokens (CSS custom properties)
- ESLint for linting

## Prerequisites

- Node.js 24 LTS (minimum 22.12) — see `.nvmrc`
- npm (bundled with Node)

## Getting started

From the repository root:

```bash
cd frontend
npm install
npm run dev
```

Then open http://localhost:5173.

## Scripts

| Command           | What it does                                   |
|-------------------|------------------------------------------------|
| `npm run dev`     | Start the dev server with hot reload           |
| `npm run build`   | Type-check and build for production to `dist/` |
| `npm run preview` | Serve the production build locally             |
| `npm run lint`    | Run ESLint on the project                      |

## Project structure

```
src/
├── main.tsx              Entry point: global styles + <App />
├── App.tsx               Page layout; owns state and passes it to components
├── api/
│   ├── types.ts          Request/response types (mirror the Java records)
│   ├── client.ts         fetch calls to the Spring Boot API
│   ├── mockApi.ts        Mock search for working without the backend
│   └── api.ts            Picks mock or real (VITE_API_MODE)
├── state/
│   ├── appState.ts       Reducer: map, tool, algorithm, seed, errors
│   └── searchView.ts     Replays search steps into grid marks, stats and messages
├── hooks/
│   ├── useSearch.ts      Re-runs the search after edits; cancels stale requests
│   └── usePlayback.ts    Step index, play/pause, end-of-replay animation
├── utils/
│   └── download.ts       Browser file download
├── styles/
│   ├── tokens.css        Design tokens: colours, fonts, radii, shadows
│   └── global.css        Reset and page-wide defaults
├── data/
│   ├── algorithms.ts     Algorithm names and descriptions
│   └── gridPresets.ts    Grid-size presets and limits
└── components/
    ├── Header/           Title, logo, Load/Save map
    ├── Logo/             2×2 brand mark
    ├── GridPanel/        Grid, legend and frame
    ├── StatCards/        Run statistics
    ├── ControlsPanel/    Controls panel, one folder per section
    │   ├── MapSection/
    │   ├── TerrainSection/
    │   ├── AlgorithmSection/
    │   └── PlaybackSection/
    ├── AlgorithmCard/    Description of the selected algorithm
    ├── ui/               Reusable controls (Button, Stepper, SegmentedControl, …)
    └── icons/            SVG icons built on a shared base component
```

## Styling

- **Design tokens** in `src/styles/tokens.css` hold every colour, font, radius
  and shadow. Components never hard-code a colour.
- **CSS Modules** (`*.module.css`) scope class names to each component, so
  styles can't leak or collide.
- **Fonts:** Bricolage Grotesque (display), Instrument Sans (UI) and
  JetBrains Mono (seeds and coordinates), loaded from Google Fonts.

## Running against the backend

`npm run dev` uses the Spring Boot backend: start it first on port 8080 (see
`docs/how-to-run.md`). Vite forwards `/api` requests to `localhost:8080`.
To work without the backend, run `npm run dev:mock` instead.

## Status

- [x] Static layout matching the design
- [x] State, event handlers and TypeScript domain types
- [x] Search playback and path animation
- [x] Connected to Spring Boot: search and random maps
- [x] Save and load through Spring Boot
- [ ] A*, Dijkstra and bidirectional search (backend)

## Toolchain notes

- **TypeScript is pinned to 6.0.x.** typescript-eslint does not yet support
  TypeScript 7's native compiler, so the template's TS 7 dependency could not
  be installed alongside ESLint. Revisit once typescript-eslint supports TS 7.