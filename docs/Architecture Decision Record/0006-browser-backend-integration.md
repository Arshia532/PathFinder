# 0006: Browser to Java integration

Status: Implemented for the integration branch.

## Context

The React layout used placeholder data and a client-side mock search. Users
need to generate a seeded map, run a Java search, and replay its expansions
from the same screen.

## Decision

- Use the existing map and search response shapes over HTTP. The Java server
  listens on loopback, and Vite proxies requests under `/api`.
- Send the seed as a decimal string so the browser can pass all signed Java
  `long` values without rounding. Both endpoints accept 10 to 50 rows and
  columns, matching the controls.
- Keep maze generation and search in Java. Maze generation uses seeded DFS
  with an explicit stack and keeps a closed border. Even dimensions preserve
  their exact size by leaving a spare wall strip. The start and end suggestions
  live beside the immutable grid.
- Group each search expansion with the new frontier cells reported by the
  existing search observer. React renders those steps and reveals the final
  path on the last frame.

## Consequences

BFS and DFS work with the maze, random maps, and manual edits. A maze provides
one route between any two open cells. A random or edited map can be unreachable,
which is displayed as a normal result. A*, Dijkstra, and bidirectional search
need their Java implementations before the UI can offer them.

## Verification

The HTTP integration test posts a seeded maze, checks reproducibility and
dimensions, and sends the returned map to BFS and DFS. Both searches must
return the same route and report frame counts consistent with their statistics.
