# 0001: Grid foundation

Status: Implemented grid foundation with four-direction movement, zero-based
coordinates, positive integer terrain costs paid on entry, and no initial cost
for the starting cell. The build targets JDK 26 and Maven 3.9.9 and is verified;
see [0002](0002-maven-build.md). Remaining product decisions are listed below.

## Context

PathFinder needs configurable rectangular grids, walls, and weighted terrain.
BFS, DFS, Dijkstra, A*, and supported bidirectional searches must share the same
map rules. The model must work independently of Spring Boot and React.

## Decision

- Keep `Grid` and `Position` in separate files in `pathfinder.grid`.
- Store walls in a flat `boolean[]` and terrain costs in a flat `int[]`.
- Use zero-based coordinates, with rows increasing downward and columns rightward.
- Map coordinates to indices using `rowIndex * columnCount + columnIndex`.
  Coordinates start at zero; `columnCount` is the total number of columns.
- Allow movement up, right, down, and left, in that order. Exclude walls.
- Require integer terrain costs from `1` to the configured maximum, capped at
  `100`. Default terrain costs `1`; the default configured maximum is `100`.
- Charge the cost of entering a cell. The starting cell adds no cost.
- Make each grid an immutable snapshot. Copy constructor inputs and expose no arrays.
- Keep endpoints, visited flags, parents, distances, and playback outside the grid.
- Write neighbors to a reusable four-entry buffer rather than allocate a list per visit.

Positions are immutable Java records. Algorithms can use integer cell indices in
their inner loops and convert to positions when preparing results for the UI.
Invalid coordinates and indices throw exceptions; they never wrap or clamp.

## Performance and limits

Constructing a grid takes O(rowCount * columnCount) time and memory. Cell lookups and
neighbor discovery take O(1) time. Discovering neighbors allocates no objects.
Each concurrent search needs its own buffer and temporary search state.

Dimensions must be positive, and their product must fit a Java integer index.
This is an indexing check, not an application memory limit: very large arrays can
still exhaust memory. The API must enforce a practical cell limit before grid
construction once the team agrees on supported sizes and playback limits.

## Implications for the next steps

- Planned React integration will submit edited map data as arrays for a new Java
  snapshot. Resizing creates a new grid; preservation of old cells is a separate
  product decision. Java owns the grid and searches and remains independently testable.
- Path length counts moves; total path cost sums entry costs. Algorithms should
  accumulate costs in `long` to avoid overflowing `int` on expensive routes.
- BFS minimizes moves, and DFS finds a route without an optimality guarantee.
  Dijkstra and A* must account for terrain costs.
- A* can use Manhattan distance multiplied by a lower bound on traversable
  terrain costs. Using `1` is a valid starting lower bound under these rules.
- Entry costs are directional. A backward search traversing from current cell
  `v` to predecessor `u` must charge the original forward edge `u -> v`, which
  costs the terrain of `v`, rather than the terrain of `u`.
- Maps may have no route, whether generated or user-created. An unreachable
  destination is a normal search outcome. The UI should explain that the
  destination cannot be reached on that map. Search
  outcomes and UI messaging are not implemented in this step.
- Blocked start or destination cells must be rejected, with a message asking
  for traversable endpoints. Validate endpoints before checking whether they are identical.
- Identical traversable endpoints return immediate success with zero moves and
  zero cost. Endpoint validation and this result belong to the search layer and
  are not implemented yet.
- No movement interfaces, terrain hierarchy, or separate object per cell is needed
  yet. Add abstractions when a concrete requirement justifies them.

## Open decisions

- Default and maximum grid dimensions, terrain categories, and preservation of
  cells on resize remain undecided.

## Verification

`backend/verify.ps1` delegates to Maven's `verify` phase, which compiles with
warnings treated as errors, runs JUnit tests, packages the domain JAR, and generates
coverage and strict JavaDocs. Tests cover rectangular coordinates, all boundaries,
one-row/one-column maps, walls, weighted
terrain, independent input copies, and invalid input including dimension overflow.

Grid and position checks are separate JUnit test files and pass on JDK 26.
See [0003](0003-terrain-limits-and-grid-equality.md) for terrain-limit validation
and grid equality semantics.
