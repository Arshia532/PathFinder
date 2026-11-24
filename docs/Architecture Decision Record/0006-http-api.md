# 0006: HTTP API with Spring Boot

Status: Implemented. `POST /api/search` runs BFS and DFS and records every step,
and the map endpoints generate, export, and import maps. Terrain costs, A*,
Dijkstra, and bidirectional search are not yet available through the API.

## Context

The frontend renders maps and replays searches, but it never runs a search
itself. It needs a way to send a map to the Java backend and receive the
search's steps, path, and statistics, and to ask the backend for random maps
and saved map files. The grid, search, and generation packages (see
[0001](0001-grid-foundation.md), [0004](0004-shared-search-engine.md), and
[0005](0005-seeded-random-generation.md)) are plain Java and should stay that
way, so that they remain easy to test and independent of any framework.

## Decision

- Use Spring Boot 4.1 (`spring-boot-starter-webmvc`) for an HTTP server on
  port 8080. `PathFinderApplication` starts it.
- Keep all Spring code in the new package `pathfinder.api`. The grid, search,
  and generation packages do not depend on Spring.
- Keep the server stateless. Every request carries the whole map, and the server
  stores nothing between requests.
- Provide four JSON endpoints:
  - `POST /api/search`: runs one search and returns `found`, `steps`, `path`,
    and `stats`.
  - `POST /api/maps/random`: generates a map from a width, height, and seed.
  - `POST /api/maps/export`: returns a map as the pretty-printed contents of a
    map file.
  - `POST /api/maps/import`: reads a map file and returns the checked map.
- Represent cells in JSON as `{"row": r, "column": c}`, the same as `Position`.
  Algorithm ids are lowercase (`"bfs"`, `"dfs"`, `"astar"`, `"dijkstra"`) and
  must match `frontend/src/api/types.ts`.
- Check every map with one shared class, `MapRules`, before it is used:
  - width and height are from 10 to 50;
  - start and end are present and inside the grid;
  - every wall is inside the grid, and duplicate walls are removed;
  - start and end are not walls, reported with the messages from
    `SearchAlgorithm`.
- Record playback steps with `StepRecorder`, a `SearchObserver`. Each step is one
  expanded cell plus the cells first discovered while it was examined. The
  start's initial discovery is not a step. Each step also has a `side`
  (`"forward"` or `"backward"`), so that bidirectional search can be added
  without changing the format.
- Report `stats` as `visited` (the number of expanded cells), `pathLength`
  (moves), and `pathCost`. The last two are `null` when no path exists.
- Return an unreachable end as a normal `200` response with `found: false`.
- Report errors as `{"message": "..."}`, written so that the frontend can show
  them as they are:
  - `400` for a rule broken by the request (`InvalidRequestException`) or a body
    that is not valid JSON or has the wrong shape;
  - `500` with a generic message for any other runtime exception, which is
    logged.
- Generate random maps with wall probability `0.30`. Place the start at
  `(height - 6, 3)` and the end at `(5, width - 4)`, and keep both cells open.
  This settles the open question in 0005: the API layer, not the generator,
  keeps the endpoints free of walls.
- Give every cell terrain cost `1` for now, because requests do not carry terrain
  yet.
- Accept `"astar"`, `"dijkstra"`, and `bidirectional: true` in requests, but
  answer them with `400` until those searches are implemented.
- Make the classes that define the JSON format (requests, responses, and their
  enums) public. Keep controllers, services, and helpers package-private.

## Consequences

- The core packages can still be built and tested without Spring. Only
  `pathfinder.api` needs a Spring context, and only its HTTP tests start one.
- The build now depends on Spring Boot and its libraries, including Jackson 3
  (`tools.jackson`). This makes the build larger and adds Maven warnings from
  those libraries.
- Because the server is stateless, every edit sends the whole map and runs the
  search again. With at most 2,500 cells, the requests stay small.
- The JSON field names are a contract between the backend and the frontend.
  Renaming a field means changing both sides at the same time.
- The default start and end of a random map are defined in both `MapService` and
  `frontend/src/state/appState.ts`, so they must be kept in sync.
- Weighted maps cannot reach the backend until requests carry terrain costs.
- The size limits are enforced by the server, so the frontend cannot bypass them.

## Verification

Tests in `pathfinder.api` check that:

- `ApiJsonTest`: the frontend's request JSON is read correctly, missing walls
  become an empty list, and `side` and `null` stats are written as expected;
- `MapRulesTest`: duplicate walls are removed in order, and a wall on the end or
  a missing end is rejected;
- `MapServiceTest`: the same seed and size give the same map, the start and end
  are never walls, and export followed by import gives the same map;
- `StepRecorderTest`: discoveries are grouped under the expanded cell, there is
  one step per expansion ending at the goal, and each cell is discovered at
  most once;
- `SearchControllerTest`: over HTTP, BFS finds the shortest path around a wall,
  an unreachable end is a normal result, and invalid sizes, walls, endpoints,
  coordinates, algorithms, and JSON are rejected with `400`;
- `MapControllerTest`: over HTTP, random maps have the requested size, sizes out
  of range are rejected, export removes duplicates, and import rejects text that
  is not a map or a map with a wall on the start.

`clean verify` passes on JDK 26.
