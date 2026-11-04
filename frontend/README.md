# PathFinder Visualizer — Frontend

React + TypeScript client for PathFinder Visualizer. The Java Spring Boot
backend (`../backend`) runs every search and returns the recorded steps;
this app draws the grid and replays those steps.

## Tech stack

- React 19 + TypeScript, built with Vite
- Plain CSS with design tokens (CSS custom properties)
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

## Toolchain notes

- **TypeScript is pinned to 6.0.x.** typescript-eslint does not yet support
  TypeScript 7's native compiler, so the template's TS 7 dependency could not
  be installed alongside ESLint. Revisit once typescript-eslint supports TS 7.