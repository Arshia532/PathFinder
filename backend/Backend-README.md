# Backend

The Java backend of PathFinder: the grid model, map generation, and pathfinding
algorithms. It is plain Java with no runtime dependencies.

| Package                 | Contents                                       | Design record                                                                                                                                                       |
| ----------------------- | ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `pathfinder.grid`       | `Grid` and `Position`: the immutable map model | [0001](../docs/Architecture%20Decision%20Record/0001-grid-foundation.md), [0003](../docs/Architecture%20Decision%20Record/0003-terrain-limits-and-grid-equality.md) |
| `pathfinder.generation` | `RandomGridGenerator`: seeded random walls     | [0005](../docs/Architecture%20Decision%20Record/0005-seeded-random-generation.md)                                                                                   |
| `pathfinder.search`     | The shared search loop, BFS, and DFS           | [0004](../docs/Architecture%20Decision%20Record/0004-shared-search-engine.md)                                                                                       |

Sources are in `src/main/java/`, and each package has matching tests in
`src/test/java/`. The build is described in
[ADR 0002](../docs/Architecture%20Decision%20Record/0002-maven-build.md).

Not yet implemented: Dijkstra, A\*, bidirectional search, playback recording,
and the Spring Boot web layer.

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
exists. It may place a wall on a cell the user later selects as the start or
destination; the team has not yet decided whether the generator or the API layer
should keep those cells open.

The design is recorded in
[ADR 0005](../docs/Architecture%20Decision%20Record/0005-seeded-random-generation.md).

## Search

`src/main/java/pathfinder/search/` finds a path between two cells of a grid. BFS
and DFS are implemented on one shared search loop, so Dijkstra and A\* can be
added as small subclasses. The reasons for this design are recorded in
[ADR 0004](../docs/Architecture%20Decision%20Record/0004-shared-search-engine.md).

```java
Grid grid = new Grid(3, 3);
SearchAlgorithm algorithm = new BreadthFirstSearch();   // or new DepthFirstSearch()
SearchResult result = algorithm.search(grid, new Position(0, 0), new Position(2, 2));

if (result.found()) {
    result.path();           // [(0,0), (0,1), (0,2), (1,2), (2,2)]
    result.moves();          // 4
    result.cost();           // 4: the sum of the terrain costs entered
    result.expandedCount();  // 9: cells expanded, shown as "nodes visited"
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

| Algorithm          | Frontier (`createFrontier`)                                            | Keep a new route? (`shouldUpdate`) | Result                                         |
| ------------------ | ---------------------------------------------------------------------- | ---------------------------------- | ---------------------------------------------- |
| BFS                | Queue: first in, first out                                             | Only the first route found         | Fewest moves                                   |
| DFS                | Stack: last in, first out                                              | Always the newest route            | A valid path, usually not the shortest         |
| Dijkstra (planned) | Priority queue: lowest cost so far first                               | Only a cheaper route               | Lowest cost                                    |
| A\* (planned)      | Priority queue: lowest cost so far plus estimated remaining cost first | Only a cheaper route               | Lowest cost, usually with fewer cells expanded |

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

On an open 3x3 grid, searching from the top-left corner to the bottom-right
corner, the numbers show the order in which cells are expanded:

```text
BFS             DFS
1 2 4           1 . .
3 5 7           2 . .
6 8 9           3 4 5
```

BFS expands all 9 cells and returns right, right, down, down. DFS expands 5 cells
and returns down, down, right, right. Both paths take 4 moves here; on maps with
walls, DFS paths are often much longer.

### Code structure

| Type                                     | Role                                                                                                |
| ---------------------------------------- | --------------------------------------------------------------------------------------------------- |
| `SearchAlgorithm`                        | Abstract base class. Contains the shared `final` search loop and declares the two `abstract` steps. |
| `BreadthFirstSearch`, `DepthFirstSearch` | Concrete algorithms. Each implements only the two steps.                                            |
| `Frontier`                               | Interface for the collection of discovered cells: `push`, `pop`, `isEmpty`.                         |
| `QueueFrontier`, `StackFrontier`         | Package-private frontiers backed by `int[]` arrays that grow when full.                             |
| `SearchResult`                           | Immutable record of the path, its cost, and the number of cells expanded.                           |
| `SearchObserver`                         | Optional listener that receives each step of a search. Playback recording will be built on it.      |

The design uses two object-oriented techniques:

- **Template Method pattern.** The base class fixes the steps of the search, and
  each subclass supplies only the parts that vary. Because `search` is `final`,
  the endpoint rules, path reconstruction, and cost accounting are identical for
  every algorithm.
- **Polymorphism through an interface.** The loop depends only on `Frontier`, so
  replacing a queue with a stack changes the search order without changing the
  loop.

Search state is held in arrays indexed by cell: `parent` (`int[]`), `cost`
(`long[]`, so large totals cannot overflow), and `expanded` (`boolean[]`). The
frontiers also store primitive `int` values, which avoids allocating an `Integer`
object for every cell, as `ArrayDeque<Integer>` would. Each search allocates its
own state, so one algorithm instance can be reused and shared between threads.
Each cell is expanded at most once, so time and memory are proportional to the
number of cells.

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
3. packages the domain JAR;
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
| Domain JAR        | `target/pathfinder-backend-0.1.0-SNAPSHOT.jar` |

Generated files are ignored by Git. The JAR contains domain logic only; there is
no web server or application entry point yet.

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
- `generation/`: reproducibility from a seed, extreme probabilities, and input
  validation.
- `search/`: the shared search rules, run against every algorithm, plus BFS,
  DFS, frontier, and result tests. `SearchFixtures` builds small test maps from
  text rows such as `"..#.."`.

A passing test suite and a coverage report show which code ran and what was
checked; they are not a proof of correctness.

### Versions

| Component     | Version                                   |
| ------------- | ----------------------------------------- |
| JDK           | 26 (verified on Eclipse Temurin 26.0.2.1) |
| Maven         | 3.9.9                                     |
| Maven Wrapper | 3.3.4                                     |
| JUnit Jupiter | 6.1.3                                     |
| JaCoCo        | 0.8.16                                    |

`pom.xml` pins the version of every dependency and build plugin so that all
contributors run the same build. JUnit is the only dependency and is used only
in tests. The build uses Maven Enforcer (Java and Maven version checks),
Compiler, Surefire (test runner), JaCoCo (coverage), and Javadoc.

Maven 3.9.9 prints warnings from its own bundled libraries when it runs on JDK
26. They do not come from this project's code and do not fail the build.

References: [Maven Wrapper](https://maven.apache.org/tools/wrapper/),
[JUnit](https://docs.junit.org/6.1.3/overview.html), and
[JaCoCo releases](https://www.jacoco.org/jacoco/trunk/doc/changes.html).
