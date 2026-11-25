# Backend

The Java backend of PathFinder: the grid model, map generation, pathfinding
algorithms, and the HTTP API that the frontend calls. The grid, generation, and
search packages are plain Java; only `pathfinder.api` and the
`PathFinderApplication` entry point use Spring Boot.

| Package                 | Contents                                       | Design record                                                                                                                                                       |
| ----------------------- | ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `pathfinder.grid`       | `Grid` and `Position`: the immutable map model | [0001](../docs/Architecture%20Decision%20Record/0001-grid-foundation.md), [0003](../docs/Architecture%20Decision%20Record/0003-terrain-limits-and-grid-equality.md) |
| `pathfinder.generation` | `RandomGridGenerator` and `MazeGenerator`: seeded random walls and mazes | [0005](../docs/Architecture%20Decision%20Record/0005-seeded-random-generation.md), [0007](../docs/Architecture%20Decision%20Record/0007-seeded-maze-generation.md) |
| `pathfinder.search`     | The shared search loop, BFS, DFS, Dijkstra, and A\* | [0004](../docs/Architecture%20Decision%20Record/0004-shared-search-engine.md), [0008](../docs/Architecture%20Decision%20Record/0008-dijkstra-and-astar.md) |
| `pathfinder.api`        | Spring Boot endpoints for search and maps      | [0006](../docs/Architecture%20Decision%20Record/0006-http-api.md)                                                                                                   |

Sources are in `src/main/java/`, and each package has matching tests in
`src/test/java/`. The build is described in
[ADR 0002](../docs/Architecture%20Decision%20Record/0002-maven-build.md).

Not yet implemented: bidirectional search and terrain costs in API requests.
How to start the server is described in [How to run](../docs/how-to-run.md).

## Grid model

`Grid` is an immutable rectangular map. It follows these rules:

- Coordinates are zero-based. Rows increase downward and columns increase to
  the right.
- Movement is in four directions: up, right, down, and left. Edges do not wrap.
- Each cell is either open or a wall. A wall cannot be entered.
- Each cell has an integer terrain cost from `1` to the grid's configured
  maximum. The maximum is between `1` and `100` and defaults to `100`. New open
  grids have cost `1` everywhere.
- Entering a cell costs its terrain cost. The start cell adds no cost.
- Walls and terrain are independent: a wall keeps its terrain cost, which must
  also be within the limit.
- A grid stores map data only. Endpoints and search state belong to the search
  layer.

`Position` is an immutable record of a row and a column. It rejects negative
coordinates but is not tied to a grid; `Grid` checks whether a position is
inside that map.

### Storage and indexing

`Grid` stores two flat arrays: a `boolean[]` of walls and an `int[]` of terrain
costs. A cell has the same index in both, calculated as
`rowIndex * columnCount + columnIndex` (row-major order). In a grid with three
rows and four columns, the indices are:

```text
 0  1  2  3
 4  5  6  7
 8  9 10 11
```

Position `(1, 2)` has index `1 * 4 + 2 = 6`. Its neighbors, when open, are
returned in the order up, right, down, left: `2`, `7`, `10`, `5`. Walls are
excluded, and a wall has no neighbors.

| Operation                         | Purpose                                                                  |
| --------------------------------- | ------------------------------------------------------------------------ |
| `rows()`, `columns()`, `size()`   | Read the dimensions and the total cell count.                            |
| `contains(rowIndex, columnIndex)` | Check bounds without throwing.                                           |
| `indexOf(position)`               | Convert a position to its cell index.                                    |
| `positionAt(index)`               | Convert a cell index to a position.                                      |
| `isBlocked(index)`                | Check whether a cell is a wall.                                          |
| `terrainCost(index)`              | Read a cell's terrain cost, including terrain under a wall.              |
| `maxTerrainCost()`                | Read the configured limit, not the largest cost present.                 |
| `neighbors(index, buffer)`        | Write the open neighbors into `buffer` and return how many were written. |
| `equals(other)`, `hashCode()`     | Compare and hash grids by value.                                         |

### Immutability and equality

The constructor copies its input arrays, and the grid has no setters and exposes
no internal arrays, so a grid never changes after construction. Editing a map
means creating a new grid.

Two grids are equal when their dimensions, configured terrain limit, walls, and
all terrain costs (including terrain under walls) match. `hashCode()` uses the
same values, so grids work correctly in `HashSet` and as `HashMap` keys. Hashes
can collide and must not be used as unique map identifiers.

### Errors

| Cause                                                                                                                                                    | Exception                   |
| -------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------- |
| Nonpositive dimensions, wrong array lengths, a terrain limit outside `1`-`100`, a cost outside the limit, or a neighbor buffer shorter than four entries | `IllegalArgumentException`  |
| A null argument                                                                                                                                          | `NullPointerException`      |
| A position or index outside the grid                                                                                                                     | `IndexOutOfBoundsException` |

Invalid values are rejected, never clamped or wrapped. The JavaDocs state the
full contract of each method.

### Performance

Creating a grid takes O(rowCount \* columnCount) time and memory. Cell lookups
and neighbor discovery take O(1) time, and neighbor discovery allocates nothing
because it writes into the caller's buffer. Comparing or hashing grids takes
O(cellCount) time in the worst case. Dimension validation prevents integer
overflow, but the application still needs an agreed cell limit to keep memory
and playback practical.

### Example

```java
import pathfinder.grid.Grid;
import pathfinder.grid.Position;

// An open grid: 3 rows, 4 columns, cost 1 everywhere.
Grid grid = new Grid(3, 4);
int start = grid.indexOf(new Position(1, 2));
int[] neighbors = new int[Grid.MAX_NEIGHBORS];
int count = grid.neighbors(start, neighbors);

for (int i = 0; i < count; i++) {   // Only the first `count` entries are valid.
    int next = neighbors[i];
    int entryCost = grid.terrainCost(next);
}

// A grid with a wall, weighted terrain, and a terrain limit of 20.
Grid weighted = new Grid(1, 3,
        new boolean[] {false, true, false}, new int[] {1, 5, 20}, 20);
```

Both arrays are in row-major order and must contain `rowCount * columnCount`
entries. Reuse one neighbor buffer within a search, and give each concurrent
search its own.

## Random generation

`RandomGridGenerator` creates a grid of random walls from a seed:

```java
Grid grid = new RandomGridGenerator().generate(21, 30, 48213L, 0.30);
```

Each cell independently becomes a wall with the given probability, so `0.30`
produces roughly, not exactly, 30% walls. The same rows, columns, seed, and
probability always produce the same walls, so sharing a seed reproduces a map.
Every cell has terrain cost `1`.

The generator does not choose endpoints and does not guarantee that a route
exists. It may place a wall where the start or destination goes; the API keeps those
cells open (see
[ADR 0006](../docs/Architecture%20Decision%20Record/0006-http-api.md)).

The design is recorded in
[ADR 0005](../docs/Architecture%20Decision%20Record/0005-seeded-random-generation.md).

## Maze generation

`MazeGenerator` carves a maze from a seed and suggests a start and destination
in opposite corners:

```java
MazeGenerator.Maze maze = new MazeGenerator().generate(21, 30, 48213L);
maze.grid();          // walls and one-cell corridors
maze.start();         // (1, 1)
maze.destination();   // (19, 29)
```

Every open cell is reachable, with exactly one route between any two of them.
The same rows, columns, and seed always produce the same maze. Every cell has
terrain cost `1`.

The design is recorded in
[ADR 0007](../docs/Architecture%20Decision%20Record/0007-seeded-maze-generation.md).

## Search

`src/main/java/pathfinder/search/` finds a path between two cells of a grid.
BFS, DFS, Dijkstra, and A\* all run on one shared search loop as small
subclasses. The design is recorded in
[ADR 0004](../docs/Architecture%20Decision%20Record/0004-shared-search-engine.md)
and
[ADR 0008](../docs/Architecture%20Decision%20Record/0008-dijkstra-and-astar.md).

```java
Grid grid = new Grid(3, 3);
SearchAlgorithm algorithm = new AStarSearch();   // or BreadthFirstSearch, DepthFirstSearch, DijkstraSearch
SearchResult result = algorithm.search(grid, new Position(0, 0), new Position(2, 2));

if (result.found()) {
    result.path();           // [(0,0), (0,1), (0,2), (1,2), (2,2)]
    result.moves();          // 4
    result.cost();           // 4: the sum of the terrain costs entered
    result.expandedCount();  // 5: cells expanded, shown as "nodes visited"
}
```

### How a search works

Every algorithm follows the same procedure: begin at the start cell, examine its
neighbors, then their neighbors, and continue until the goal is reached or no
cells remain. The algorithms differ only in **which cell they examine next**.
The code uses four terms:

| Term     | Meaning                                                                                         |
| -------- | ----------------------------------------------------------------------------------------------- |
| Frontier | The cells that have been discovered but not yet expanded.                                       |
| Discover | Reach a cell for the first time and add it to the frontier.                                     |
| Expand   | Remove a cell from the frontier and examine its neighbors.                                      |
| Parent   | The cell from which a cell was reached. Following parents back from the goal rebuilds the path. |

The shared loop in `SearchAlgorithm.search` is:

```text
add the start to the frontier
while the frontier is not empty:
    take the next cell from the frontier
    if it was already expanded, skip it (an outdated entry)
    mark it expanded; if it is the goal, follow the parents back and return the path
    for each open neighbor (up, right, down, left) that is not expanded:
        if the algorithm keeps this route to the neighbor:
            record its parent and cost, and add it to the frontier
the frontier is empty: the goal cannot be reached
```

Each algorithm defines its behavior by overriding two methods:

| Algorithm | Frontier (`createFrontier`)                                            | Keep a new route? (`shouldUpdate`) | Result                                         |
| --------- | ---------------------------------------------------------------------- | ---------------------------------- | ---------------------------------------------- |
| BFS       | Queue: first in, first out                                             | Only the first route found         | Fewest moves                                   |
| DFS       | Stack: last in, first out                                              | Always the newest route            | A valid path, usually not the shortest         |
| Dijkstra  | Priority queue: lowest cost so far first                               | Only a cheaper route               | Lowest cost                                    |
| A\*       | Priority queue: lowest cost so far plus estimated remaining cost first | Only a cheaper route               | Lowest cost, usually with fewer cells expanded |

**BFS** expands cells in order of distance from the start: every cell one move
away, then every cell two moves away, and so on. The first route that reaches the
goal therefore has the fewest moves. BFS ignores terrain cost when choosing a
route, but reports the cost of the path it returns.

**DFS** always expands the most recently discovered cell, so it follows one route
until it reaches a dead end, then returns to the most recent cell that still has
an unexplored neighbor. It finds a path whenever one exists, but that path is
often long. Neighbors are pushed in the order up, right, down, left, so they are
tried in the reverse order: left, down, right, up. When DFS reaches a cell that
is already waiting on the stack, it keeps the new route and pushes the cell
again, so the cell is expanded next; the older entry is skipped later. This is
why its `shouldUpdate` always returns `true`.

**Dijkstra** expands the unexpanded cell with the lowest total terrain cost
`g(n)` from the start, using a binary min-heap. Because every entered cell has a
positive terrain cost, the first time a cell is expanded its recorded cost is
already minimal, so the path returned always has the lowest possible cost. When
every cell has the same terrain cost, equal-cost ties break in discovery order
and Dijkstra expands the same cells as BFS.

**A\*** uses the same min-heap and route-replacement rule as Dijkstra, but
orders cells by `f(n) = g(n) + h(n)`, where `h(n)` is the Manhattan distance
`|r1 - r2| + |c1 - c2|` to the goal multiplied by `1` (the minimum terrain
cost). Because orthogonal movement takes at least that many steps and each step
costs at least `1`, `h(n)` never overestimates the true remaining cost and
changes by at most `1` per move. Every expanded cell's cost is therefore final,
and A\* finds a lowest-cost path while expanding only cells that can beat or tie
the optimal cost. Equal-`f` ties prefer the cell with the larger `g(n)` (smaller
remaining `h(n)`), pulling the search toward the goal on uniform terrain.

On an open 3x3 grid (cost `1` everywhere), searching from the top-left corner to
the bottom-right corner, the numbers show the order in which cells are expanded:

```text
BFS / Dijkstra  DFS             A*
1 2 4           1 . .           1 2 3
3 5 7           2 . .           . . 4
6 8 9           3 4 5           . . 5
```

BFS and Dijkstra expand all 9 cells; DFS expands 5 cells down the left edge; A\*
expands 5 cells straight along a shortest path. On weighted terrain, Dijkstra
and A\* detour around expensive cells when doing so lowers the total cost, while
BFS still picks the path with the fewest moves.

### Code structure

| Type                                                                       | Role                                                                                                  |
| -------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| `SearchAlgorithm`                                                          | Abstract base class. Contains the shared `final` search loop and declares the two `abstract` steps.   |
| `BreadthFirstSearch`, `DepthFirstSearch`, `DijkstraSearch`, `AStarSearch`  | Concrete algorithms. Each implements only the two steps.                                              |
| `Frontier`                                                                 | Interface for the collection of discovered cells: `push`, `pop`, `isEmpty`.                           |
| `QueueFrontier`, `StackFrontier`, `PriorityFrontier`                       | Package-private frontiers backed by primitive `int[]` and `long[]` arrays that grow when full.        |
| `SearchResult`                                                             | Immutable record of the path, its cost, and the number of cells expanded.                             |
| `SearchObserver`                                                           | Optional listener that receives each step of a search. The API's `StepRecorder` uses it for playback. |

The design uses two object-oriented techniques:

- **Template Method pattern.** The base class fixes the steps of the search, and
  each subclass supplies only the parts that vary. Because `search` is `final`,
  the endpoint rules, path reconstruction, and cost accounting are identical for
  every algorithm.
- **Polymorphism through an interface.** The loop depends only on `Frontier`, so
  replacing a queue with a stack or a binary min-heap changes the search order
  without changing the loop.

Search state is held in arrays indexed by cell: `parent` (`int[]`), `cost`
(`long[]`, so large totals cannot overflow), and `expanded` (`boolean[]`). The
frontiers also store primitive `int` and `long` values, which avoids allocating
wrapper objects for every push, as `ArrayDeque<Integer>` or `PriorityQueue`
would. Each search allocates its own state, so one algorithm instance can be
reused and shared between threads. Each cell is expanded at most once, so memory
is proportional to the number of cells; BFS and DFS run in O(cells) time, and
Dijkstra and A\* run in O(cells log cells) time.

A `SearchObserver` passed to `search` is told when each cell is discovered and
when it is expanded. Every cell is discovered at most once, always before it is
expanded, and expanded at most once, for every algorithm.

### Search rules

- A start or destination outside the grid causes `IndexOutOfBoundsException`.
- A start on a wall causes `IllegalArgumentException` with the message
  `SearchAlgorithm.START_BLOCKED_MESSAGE`; a destination on a wall uses
  `SearchAlgorithm.DESTINATION_BLOCKED_MESSAGE`. When both are walls, the start
  is reported. Walls are checked before the next rule.
- Identical traversable endpoints succeed with a one-cell path, zero moves, and
  zero cost.
- An unreachable destination is a normal result, not an error: `found()` is
  `false` and the path is empty. The UI should explain that the destination
  cannot be reached on that map.

## Build and verification

The build requires JDK 26. The Maven wrapper downloads and caches Maven 3.9.9 on
first use and checks its SHA-256, so no separate Maven installation is needed.
The first build needs a network connection to download Maven and the
dependencies. The build rejects other Java and Maven versions.

From the repository root in PowerShell:

```powershell
./backend/verify.ps1
```

The script runs Maven's `verify` phase, which:

1. compiles the code, treating every compiler warning as an error;
2. runs the JUnit tests, and fails if no tests are found;
3. packages the runnable JAR;
4. generates the coverage report;
5. generates the JavaDocs, treating every JavaDoc warning as an error.

To run Maven directly:

```powershell
cd backend
.\mvnw.cmd test            # unit tests only
.\mvnw.cmd verify          # the full check
.\mvnw.cmd clean verify    # the full check from a clean output directory
```

On macOS and Linux, use `./mvnw` in place of `.\mvnw.cmd`.

| Output            | Location, relative to `backend/`               |
| ----------------- | ---------------------------------------------- |
| JUnit results     | `target/surefire-reports/`                     |
| Coverage report   | `target/site/jacoco/index.html`                |
| API documentation | `target/site/apidocs/index.html`               |
| Runnable JAR      | `target/pathfinder-backend-0.1.0-SNAPSHOT.jar` |

Generated files are ignored by Git. The JAR is a Spring Boot application that
starts the API server on port 8080 with
`java -jar target/pathfinder-backend-0.1.0-SNAPSHOT.jar`.

### Selecting JDK 26

In an IDE, open `backend/pom.xml` as a Maven project and select JDK 26 for both
the project SDK and the Maven runner. If a terminal still uses an older JDK,
point the current PowerShell session at JDK 26:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-26.0.2.101-hotspot'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version
```

Replace the path with your own installation directory. This affects only the
current terminal and does not change the system-wide Java default.

### Tests

Each package has its own tests under `src/test/java/pathfinder/`:

- `grid/`: coordinates, neighbors, walls, terrain limits, equality, and input
  validation.
- `generation/`: reproducibility from a seed, extreme probabilities, maze
  connectivity and shape, and input validation.
- `search/`: the shared search rules, run against every algorithm, plus BFS,
  DFS, Dijkstra, A\*, frontier, and result tests. `SearchFixtures` builds small
  test maps from text rows such as `"..#.."`.
- `api/`: JSON reading and writing, map rules, step recording, and the
  endpoints over HTTP.

A passing test suite and a coverage report show which code ran and what was
checked; they are not a proof of correctness.

### Versions

| Component     | Version                                   |
| ------------- | ----------------------------------------- |
| JDK           | 26 (verified on Eclipse Temurin 26.0.2.1) |
| Maven         | 3.9.9                                     |
| Maven Wrapper | 3.3.4                                     |
| Spring Boot   | 4.1.1                                     |
| JUnit Jupiter | 6.1.3                                     |
| JaCoCo        | 0.8.16                                    |

`pom.xml` pins the version of every dependency and build plugin so that all
contributors run the same build. The only runtime dependency is Spring Boot's web
starter, used only by `pathfinder.api` and `PathFinderApplication`. JUnit and
Spring Boot's test support are used only in tests. The build uses Maven Enforcer (Java and Maven version
checks), Compiler, Surefire (test runner), JaCoCo (coverage), Javadoc, and the
Spring Boot plugin (runnable JAR).

Maven 3.9.9 prints warnings from its own bundled libraries when it runs on JDK
26. They do not come from this project's code and do not fail the build.

References: [Maven Wrapper](https://maven.apache.org/tools/wrapper/),
[JUnit](https://docs.junit.org/6.1.3/overview.html), and
[JaCoCo releases](https://www.jacoco.org/jacoco/trunk/doc/changes.html).
