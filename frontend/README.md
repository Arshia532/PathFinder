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
├── App.tsx               Page layout (two columns)
├── styles/
│   ├── tokens.css        Design tokens: colours, fonts, radii, shadows
│   └── global.css        Reset and page-wide defaults
├── data/
│   ├── algorithms.ts     Algorithm names and descriptions
│   ├── gridPresets.ts    Grid-size presets and limits
│   └── placeholder.ts    Temporary static data (replaced by live state)
└── components/
    ├── Header/           Title and logo
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

## Status

- [x] Static layout matching the design, using placeholder data
- [ ] State, event handlers and TypeScript domain types
- [ ] Connect to the Spring Boot API and replay search steps

## Toolchain notes

- **TypeScript is pinned to 6.0.x.** typescript-eslint does not yet support
  TypeScript 7's native compiler, so the template's TS 7 dependency could not
  be installed alongside ESLint. Revisit once typescript-eslint supports TS 7.