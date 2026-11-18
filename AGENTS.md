# PathFinder

Web app that generates or edits grid maps and visualizes pathfinding algorithms
frame by frame. Java backend (map model, generation, search). React + TypeScript
frontend (rendering and playback only; it never runs a search itself).

## Layout

- `backend/`: one Maven module, plain Java.
  - `pathfinder.grid`: `Grid` (immutable map of walls and terrain costs), `Position`.
  - `pathfinder.generation`: seeded, repeatable map generation.
  - `pathfinder.search`: `SearchAlgorithm` (shared search loop) and the algorithms built on it.
- `frontend/`: Vite + React + TypeScript.
- `docs/Architecture Decision Record/`: numbered design decisions. Read the relevant one before changing a design.

## Commands

Backend, from `backend/` (JDK 26 required; the wrapper downloads Maven 3.9.9):

```powershell
.\mvnw.cmd -B test           # unit tests
.\mvnw.cmd -B clean verify   # full check: build, tests, coverage, strict JavaDoc
```

On macOS/Linux use `sh ./mvnw`. Run `verify` before calling backend work done.

Frontend, from `frontend/` (Node version in `.nvmrc`):

```bash
npm install
npm run dev     # dev server
npm run build   # type-check and production build
npm run lint
```

## Build rules

- Only JDK 26 passes; the build rejects other Java versions.
- Compiler warnings fail the build.
- JavaDoc is checked strictly: every public type and member needs complete JavaDoc.
- Maven prints some JDK 26 warnings from its own libraries. They are expected; do not suppress them.

## Backend design rules

- Cells are flat row-major indices (`row * columns + column`), zero-based, never wrapping.
- Movement is four-direction; neighbors come in the order up, right, down, left. Walls are skipped.
- Terrain cost is paid on entering a cell (the start is free): integers from 1 to the grid's maximum, capped at 100.
- `Grid` is immutable. Endpoints, visited state, distances and paths belong to the search layer.
- Search code uses `int` indices and primitive arrays, and avoids allocating per step.
  Path costs are accumulated in `long`.
- A new algorithm subclasses `SearchAlgorithm` rather than copying the search loop.
- An unreachable destination is a normal result, not an error.

## Conventions

- Commit messages follow Conventional Commits, e.g. `feat(search): add Dijkstra`.
- Work on a feature branch and merge into `main` through a pull request.
- A significant design decision gets a new ADR that copies the structure of the latest one.
- Keep documentation simple, professional and concise.
- Do not change the line endings of `backend/mvnw` (LF) or `backend/mvnw.cmd` (CRLF).
