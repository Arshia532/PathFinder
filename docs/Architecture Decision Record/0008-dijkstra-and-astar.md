# 0008: Dijkstra and A* search

Status: Implemented. `DijkstraSearch` and `AStarSearch` run on the shared search
loop in `pathfinder.search`, and `POST /api/search` serves `"dijkstra"` and
`"astar"`. Terrain costs in API requests and bidirectional search are not
implemented yet.

## Context

`SearchAlgorithm` (see [0004](0004-shared-search-engine.md)) already provides
the shared search loop, lazy deletion of outdated frontier entries, `long` cost
accumulation, and step observation for BFS and DFS. PathFinder also needs
Dijkstra's algorithm and A* so that searches respect weighted terrain (see
[0001](0001-grid-foundation.md)) and show how a heuristic focuses exploration
toward the goal, both in direct Java callers and through `POST /api/search` (see
[0006](0006-http-api.md)).

## Decision

- Add two `public final` subclasses of `SearchAlgorithm` in `pathfinder.search`,
  matching the naming and two-method structure of `BreadthFirstSearch` and
  `DepthFirstSearch`:
  - `DijkstraSearch`: uses a min-heap frontier ordered by path cost `g(n)` and
    replaces a recorded route only when `candidateCost < knownCost`.
  - `AStarSearch`: uses the same route-replacement rule and a min-heap frontier
    ordered by `f(n) = g(n) + h(n)`, where `h(n)` is the Manhattan distance
    `|r1 - r2| + |c1 - c2|` to `target` multiplied by `1`, the minimum terrain
    cost allowed by `Grid`.
- Compute row and column coordinates in `AStarSearch` directly from the flat
  row-major cell indices (`cell / columns` and `cell % columns`) rather than
  allocating `Position` objects during the search.
- Add package-private `PriorityFrontier`, a zero-based binary min-heap backed by
  growable primitive arrays (`int[]` and `long[]`) and parameterized by an
  `IntToLongFunction` heuristic (`0` for Dijkstra). No wrapper objects or heap
  node instances are allocated per push.
- Break equal-priority ties deterministically in `PriorityFrontier`:
  1. lowest total priority `f(n) = g(n) + h(n)`;
  2. highest known path cost `g(n)` (which is a no-op tie for Dijkstra where
     `f(n) == g(n)`, and prefers smaller remaining `h(n)` for A* so searches on
     uniform-cost maps advance straight toward the goal instead of expanding a
     full bounding rectangle);
  3. earliest insertion order (FIFO), so unweighted Dijkstra expands cells in
     the exact same order as BFS and equal A* candidates follow the grid's
     neighbor order.
- Enable `AlgorithmId.DIJKSTRA` and `AlgorithmId.ASTAR` in `SearchService`.

## Consequences

- Because every entered cell has terrain cost at least `1`, Manhattan distance
  never overestimates the remaining cost (admissible) and changes by at most `1`
  between orthogonal neighbors (consistent). Every cell's cost is therefore
  already minimal the first time it is popped, so `SearchAlgorithm`'s `expanded`
  guard never needs to reopen an expanded cell.
- For every non-goal cell `u`, `h(u) >= 1`, so any cell expanded by A* satisfies
  `g(u) <= optimalCost - 1 < optimalCost` and must also be expanded by Dijkstra.
  A* therefore never expands more cells than Dijkstra on any map, and usually
  expands far fewer.
- Each search uses O(cells) memory and O(cells log cells) time. A cell can be
  pushed again when a cheaper route is found before expansion (at most four
  times, once from each neighbor), and outdated copies are skipped in O(1) when
  popped.
- Search instances remain stateless and safe to share across threads.
- Until `SearchRequest` carries per-cell terrain costs, `POST /api/search` runs
  Dijkstra and A* on cost-`1` maps, where Dijkstra matches BFS and A* explores
  toward the destination.

## Verification

- `SearchAlgorithmTest` runs all shared endpoint, observer, reuse, large-grid,
  and random-map validity checks against BFS, DFS, Dijkstra, and A*.
- `FrontierTest` checks `PriorityFrontier` ordering across array growth,
  tie-breaking on priority, cost, and insertion order, empty-heap errors, and
  20,000 random operations against `java.util.PriorityQueue`.
- `DijkstraSearchTest` checks detours around expensive terrain, identical
  expansion order to BFS on uniform random maps, nondecreasing expansion costs,
  and exact optimal costs against an independent Bellman-Ford solver across 300
  seeded random weighted maps with terrain costs up to 100.
- `AStarSearchTest` checks straight-line focus on open grids, weighted detours,
  route relaxation before expansion (with single discovery notification), and
  exact agreement with Dijkstra and Bellman-Ford across 300 seeded random
  weighted maps while expanding no more cells than Dijkstra on every map.
- `SearchControllerTest` checks `"dijkstra"` and `"astar"` over HTTP, verifies
  that A* visits fewer cells around a wall, and checks that bidirectional search
  still returns `400`.
- `clean verify` passes on JDK 26.
