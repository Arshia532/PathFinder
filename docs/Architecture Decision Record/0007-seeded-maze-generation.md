# 0007: Seeded maze generation

Status: Implemented. `MazeGenerator` creates a maze from a seed, and
`POST /api/maps/maze` returns one. The frontend does not request mazes yet.

## Context

The random generator (see [0005](0005-seeded-random-generation.md)) often
produces maps without a route, and its scattered walls do not show how the
algorithms differ in narrow corridors. PathFinder also needs a map where a route
always exists. Like a random map, a maze must be reproducible from its settings
and must produce a `Grid` that follows the rules in
[0001](0001-grid-foundation.md).

## Decision

- Add `MazeGenerator` in the package `pathfinder.generation`, with one method:
  `generate(rowCount, columnCount, seed)`.
- Carve the maze with a depth-first backtracker that uses an explicit `int`
  stack instead of recursion, so large grids cannot overflow the call stack.
- Start with every cell a wall. Rooms are the cells with an odd row and an odd
  column. Carving opens a room and the wall cell between it and a neighboring
  room. Cells with an even row and an even column always stay walls, so every
  corridor is one cell wide.
- Keep the top and left borders closed. On an odd dimension the bottom or right
  border is also closed. On an even dimension the last rooms sit on that edge,
  so corridors run to it instead of leaving a wall two cells thick. The grid
  edge still blocks movement, because neighbors never wrap.
- Use `java.util.Random` created from the seed, and choose among the unvisited
  neighboring rooms with `nextInt`. As in 0005, the same arguments give the same
  maze on every JDK and operating system.
- Give every cell terrain cost `1`, and use the grid's default terrain limit.
- Return a `Maze` record with the grid and a suggested start and destination:
  the first room `(1, 1)` and the last room in the opposite corner. This is a
  deliberate exception to 0005, where the generator chooses no endpoints. Only
  the maze generator knows which cells are corridors, and the fixed spots that
  the API uses for random maps (see [0006](0006-http-api.md)) would often fall
  on a wall. Opening such a wall would leave an unreachable pocket or add a
  loop. The `Grid` itself still holds no endpoints.
- Require at least three rows and three columns, and a cell count that fits an
  `int`. Invalid arguments cause `IllegalArgumentException`.
- Add `POST /api/maps/maze`. It takes the same body as `POST /api/maps/random`
  (`width`, `height`, `seed`), applies the same size limits, and returns a map
  with the maze's suggested start and end.

## Consequences

- Every open cell is reachable, and there is exactly one route between any two
  open cells, so BFS and DFS find the same path. The searches still differ in
  how many cells they visit.
- A maze is identified by three values: row count, column count, and seed.
- On an even dimension, the bottom or right side has no wall frame. The grid
  edge works as one.
- Grids with the same number of rooms in each direction get the same maze for
  the same seed. For example, 7 by 9 and 6 by 8 share their rooms.
- Generation takes O(rowCount * columnCount) time and memory and allocates
  nothing per step.
- The frontend only needs a control that calls the new endpoint to show mazes.

## Verification

`MazeGeneratorTest` checks that:

- the open cells form one connected tree, with no loops, across odd, even, and
  narrow sizes and several seeds;
- the same arguments reproduce the same maze, regardless of earlier calls or of
  which generator instance is used, and different seeds give different mazes;
- a fixed seed produces a known, hard-coded maze for an odd and an even size;
- the borders are closed on odd sides, and corridors reach the edge on even
  sides;
- cells with an even row and an even column are always walls;
- the markers sit in opposite corners, and coincide in a 3 by 3 maze;
- every cell has terrain cost `1`;
- dimensions that are too small or overflow `int` indexing are rejected.

`MapServiceTest` and `MapControllerTest` check that `POST /api/maps/maze`
returns a reproducible maze of the requested size, that its start and end are
open corners joined by a route, and that sizes out of range are rejected.

`clean verify` passes on JDK 26.
