# 0004: Shared search engine, BFS, and DFS

Status: Implemented. Breadth-first and depth-first search run on one shared
search loop. Dijkstra, A*, and bidirectional search are planned, not implemented.

## Context

PathFinder needs BFS, DFS, Dijkstra, and A* on the same grid rules (see
[0001](0001-grid-foundation.md)). These algorithms share the same loop: take a
cell from a collection of discovered cells, examine its neighbors, and record
how each neighbor was reached. Writing that loop once per algorithm would
duplicate the endpoint rules, path reconstruction, and cost accounting, and
every fix would have to be repeated in each copy.

## Decision

- Put the loop in one `final` method, `SearchAlgorithm.search`, in the new
  package `pathfinder.search` (Template Method pattern). Subclasses supply two
  steps and cannot change the loop:
  - `createFrontier(grid, target)`: the collection of discovered cells. Its
    pop order decides which cell is expanded next.
  - `shouldUpdate(candidateCost, knownCost)`: whether a newly found route to an
    unexpanded cell replaces the recorded one.
- Define `Frontier` as an interface with `push(cell, cost)`, `pop()`, and
  `isEmpty()`. Order-based frontiers ignore `cost`; a future priority frontier uses it.
- BFS uses a queue and keeps the first route found (`knownCost == UNREACHED`).
  DFS uses a stack and always takes the newest route, which puts the cell on top of
  the stack so that the search goes deeper.
- Test for the goal when a cell is *expanded*, not when it is discovered. This is
  required for Dijkstra and A* to be correct, and it keeps the visualized steps
  consistent across algorithms.
- Skip outdated frontier entries with an `expanded[]` flag (lazy deletion)
  rather than removing them from the frontier.
- Store per-search state in primitive arrays (`int[] parent`, `long[] cost`,
  `boolean[] expanded`) and implement the frontiers on growable `int[]` arrays.
  This avoids boxing every cell index into an `Integer`, following the
  allocation-free style of `Grid.neighbors`.
- Accumulate path costs in `long` per cell. The goal's stored cost is the
  path's cost, so no recomputation is needed. BFS and DFS ignore cost when
  choosing a path but still report the cost of the path they find.
- Report each discovered and expanded cell to an optional `SearchObserver`
  (no-op by default). Frame recording for playback will be an observer, so the
  loop does not change when playback is added. A cell is reported as discovered
  only the first time it enters the frontier, even when an algorithm such as DFS
  later replaces its route and pushes it again. Every algorithm therefore gives
  playback the same simple sequence: discovered once, then expanded at most once.
- Return a `SearchResult` record (path, cost, expanded count). Enforce the endpoint
  rules confirmed in 0001: endpoints on walls are rejected with
  `IllegalArgumentException`, using a separate message for a blocked start and a
  blocked destination (the start is reported when both are blocked), identical
  endpoints succeed with zero moves and cost, and an unreachable goal is an
  empty-path result, not an exception.

## Consequences

- Each algorithm is a small subclass. BFS and DFS each contain two short methods.
- Dijkstra: a binary-heap `PriorityFrontier` keyed by `cost`, with
  `shouldUpdate` returning `candidateCost < knownCost`.
- A*: the same, but `createFrontier` returns a frontier that adds the Manhattan
  distance to `target` (multiplied by the minimum terrain cost, `1`) to each
  priority. The `target` parameter exists for this.
- Bidirectional search runs two frontiers that must meet in the middle, so it
  does not fit a single-frontier loop. It will need its own driver, which can reuse
  the frontier classes. Backward costs must charge the forward edge (see 0001).
- DFS can push the same cell several times, so its stack may hold up to about four
  times the number of cells. Each cell is still expanded at most once.
- Search instances hold no fields, so one instance can serve many searches,
  including concurrent ones.
- Each search takes O(cells) time and memory for BFS and DFS, because each cell is
  expanded at most once and has at most four neighbors.

## Verification

- `SearchAlgorithmTest` runs the shared rules against every algorithm as JUnit
  parameterized tests: invalid arguments (including which wall message is used),
  identical endpoints, unreachable goals, instance reuse, and a 200 by 200 grid.
- Path validity (adjacent, wall-free steps with a correctly summed cost) is
  checked on 200 seeded random weighted maps, and the observer contract on 100
  more.
- BFS move counts are compared with an independently computed distance table on
  300 seeded random maps with random endpoints.
- DFS paths and expansion orders are compared with a recursive DFS written
  separately in the tests, on 300 seeded random maps.
- Frontier and result tests cover ordering, growth, and validation.
- `clean verify` passes on JDK 26.
