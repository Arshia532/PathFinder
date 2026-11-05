# Backend

Java backend for the domain model, pathfinding algorithms, and map generation.

The grid foundation is implemented as plain Java. Spring Boot and the search
algorithms have not been added yet.

## Grid model

- `src/main/java/pathfinder/grid/Grid.java`: immutable map, walls, terrain costs,
  coordinate conversion, and four-direction neighbors.
- `src/main/java/pathfinder/grid/Position.java`: immutable row and column value.
- `src/test/java/pathfinder/grid/GridTest.java`: grid behavior and validation tests.
- `src/test/java/pathfinder/grid/PositionTest.java`: position value and validation tests.

The grid uses four-direction movement, zero-based coordinates, positive integer
terrain costs paid on entry, and no initial cost for the starting cell.
New open grids have terrain cost `1` everywhere. A grid accepts a configured
maximum terrain cost from `1` to `100`, inclusive; the default maximum is `100`.
Every cell's cost, including terrain under walls, must be within that limit.
Walls and terrain are independent. Search state and endpoints belong outside
the map.

See [the grid design decision](../docs/Architecture%20Decision%20Record/0001-grid-foundation.md)
for the rules, performance characteristics, and open integration decisions.

## How the grid works

`Position` is an immutable Java record containing a row and a column. It rejects
negative coordinates. A position can exist independently of a grid; `Grid`
checks whether it is inside that particular map. Records provide coordinate-based
equality and consistent hashing automatically.

`Grid` stores two flat arrays: a `boolean[]` for walls and an `int[]` for terrain
costs. Each cell uses the same index in both arrays. An index is calculated as
`rowIndex * columnCount + columnIndex`. Here, `rowIndex` and `columnIndex` are
zero-based coordinates, while `columnCount` is the total number of columns.
For example, in a three-row, four-column grid:

```text
 0  1  2  3
 4  5  6  7
 8  9 10 11
```

Position `(1, 2)` has index `1 * 4 + 2 = 6`: skip one complete row of four cells,
then move two cells into the next row. The column count is `4`; the last column's
index is `3`. The cell's neighbors, when open, are indices `2`, `7`, `10`, and
`5`: up, right, down, left. Edges never wrap to the next row. Walls are
excluded, and a wall used as the source returns no neighbors.

| Operation | Purpose |
| --- | --- |
| `rows()`, `columns()`, `size()` | Read dimensions and total cell count. |
| `contains(rowIndex, columnIndex)` | Check bounds without throwing for invalid coordinates. |
| `indexOf(position)` | Convert a valid position to its cell index. |
| `positionAt(index)` | Convert a valid index to a new position. |
| `isBlocked(index)` | Check whether the cell is a wall. |
| `terrainCost(index)` | Read its positive terrain cost, including terrain under walls. |
| `maxTerrainCost()` | Read the configured upper bound, not the largest cost present. |
| `neighbors(index, buffer)` | Write open neighbors and return how many were written. |
| `equals(other)`, `hashCode()` | Compare and hash grid values consistently. |

Construction copies the supplied arrays, so changing the original inputs cannot
change an existing grid. There are no editing setters or exposed internal arrays.
Map edits produce a new snapshot. Start/destination selection, visited flags,
distances, and paths belong to the search layer rather than the map.

Creating a grid takes O(rowCount * columnCount) time and memory. Cell lookups and
neighbor discovery take O(1) time. Neighbor discovery reuses the caller's buffer
and creates no objects. Comparing distinct grids and computing a hash take
O(cellCount) time in the worst case because they examine the cell arrays.
Dimension validation prevents integer overflow, but an application cell limit
still needs to be agreed upon to keep memory and playback practical.

Nonpositive dimensions, wrong array lengths, invalid terrain limits or cell
costs, and short neighbor buffers cause `IllegalArgumentException`. Limits
outside `1-100` are rejected rather than silently clamped. Null required inputs
cause `NullPointerException`; out-of-bounds positions or indices cause
`IndexOutOfBoundsException`. Full contracts are documented in the JavaDocs.

## Example

```java
import pathfinder.grid.Grid;
import pathfinder.grid.Position;

Grid grid = new Grid(3, 4);
int start = grid.indexOf(new Position(1, 2));
int[] neighbors = new int[Grid.MAX_NEIGHBORS];
int count = grid.neighbors(start, neighbors);

for (int i = 0; i < count; i++) {
    int next = neighbors[i];
    int entryCost = grid.terrainCost(next);
    // The search algorithm uses next and entryCost here.
}
```

To create a map with walls and terrain, prepare a `boolean[]` and a positive
`int[]` with `rowCount * columnCount` entries each. Fill the cost array with `1`
before assigning any difficult terrain. Pass them to
`new Grid(rowCount, columnCount, walls, costs)`. Both arrays use row-major order
and are copied by the constructor. Later changes to those inputs do not change
the grid.

To select a lower terrain limit, pass it as the final constructor argument:

```java
Grid open = new Grid(3, 4, 20);
Grid weighted = new Grid(1, 3,
        new boolean[] {false, true, false}, new int[] {1, 5, 20}, 20);
```

Both grids have a configured maximum of `20`; the open grid still starts with
cost `1` everywhere. A cell cost of `21` is invalid on these grids. A configured
maximum of `101` is always invalid. The original constructors remain available
and use the default maximum of `100`.

Read only the first `count` entries in the neighbor buffer. Reuse the buffer
within one search; give separate searches their own buffers.

## Grid equality

Two grids are equal when their row and column counts, configured terrain limit,
wall positions, and all terrain costs match. Array contents are compared, so
separately constructed snapshots can be equal. Terrain under walls is included.
Different configured limits mean different grid values, even when current cell
contents match.

`hashCode()` uses those same values, allowing equal grids to work consistently
in `HashSet` and as `HashMap` keys. Hashes can collide and must not be used as
unique map identifiers. No application cache or deduplication feature is added
by these methods.

A custom `toString()` is not implemented. It could provide a small debugging
summary later; printing a full grid or serializing it for the frontend should
use a deliberately designed representation.

Terrain costs above `100` that earlier versions accepted now cause an exception.
Existing constructor signatures remain available, but callers must supply costs
within the new range. Grid comparisons now use values rather than object identity.

## Verify locally

Use JDK 26. Maven's build rules reject other Java major versions. In your IDE,
select JDK 26 for both the project SDK and Maven runner/importer. Open
`backend/pom.xml` as a Maven project to load the dependencies and tests.

The existing terminal may still select an older JDK after installation. For the
current PowerShell session, set the installation path and put its tools first:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-26.0.2.101-hotspot'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version
javac -version
```

Use your own installation directory if it differs. These assignments affect the
current terminal only. No system-wide Java default is changed by the project.

From the repository root in PowerShell:

```powershell
./backend/verify.ps1
```

The script delegates to Maven's `verify` phase. It compiles with warnings treated
as errors, runs JUnit tests, packages the domain code, generates coverage reports,
and checks JavaDocs strictly. Generated files are ignored by Git.

Test sources in `src/test/` are included with production code in version control.
The suite stays in two files: grid tests and position tests, covering
the implemented contracts without a custom test framework. This project fails
verification if no tests are discovered, so a missing suite cannot silently pass.
Generated test results and coverage reports remain ignored.

For direct Maven commands, open the backend directory:

```powershell
cd backend
.\mvnw.cmd -version
.\mvnw.cmd test
.\mvnw.cmd verify
```

`test` runs unit tests. `verify` also packages the code and generates coverage
and JavaDocs. Use `clean verify` when you need a build from fresh output. On
macOS/Linux use `sh ./mvnw` in place of `.\mvnw.cmd` (or `./mvnw` if executable).
The wrapper downloads and caches Maven 3.9.9; the first build also downloads
dependencies. An initial network connection is required. No separate Maven
installation is needed. The wrapper checks the Maven distribution's SHA-256.

| Output | Location, relative to `backend/` |
| --- | --- |
| JUnit results | `target/surefire-reports/` |
| Coverage report | `target/site/jacoco/index.html` |
| API documentation | `target/site/apidocs/index.html` |
| Domain JAR | `target/pathfinder-backend-0.1.0-SNAPSHOT.jar` |

The JAR currently contains domain logic; a web server and application entry point
have not been added. Coverage describes executed code, not proof of correctness.

## Confirmed search rules (not yet implemented)

Generated and user-created maps may have no route to the destination. This is an
expected outcome, with a message explaining that the destination cannot be
reached on that map.

Reject a search if either endpoint is blocked, with a message asking the user to
choose traversable cells. If both endpoints are the same traversable cell,
return immediate success with zero moves and zero cost. Validate endpoints before
checking whether they are identical.

These rules belong to the search layer. Searches and result messages are not
implemented yet.

## Build versions

The backend targets JDK 26 and Maven 3.9.9. Apache's generated wrapper selects
the Maven version, while the project-owned `pom.xml` pins dependencies and build
plugins. This allows contributors to reproduce the same build.

| Component | Version |
| --- | --- |
| Verified local JDK | Eclipse Temurin 26.0.2.1 |
| Maven | 3.9.9 |
| Maven Wrapper | 3.3.4, generated from Apache's plugin |
| JUnit Jupiter | 6.1.3 |
| JaCoCo | 0.8.16 |

The grid has no third-party runtime dependencies. JUnit is used only in tests.
Maven Enforcer checks the Java and Maven versions, Compiler compiles the source,
Surefire runs JUnit tests, JaCoCo records coverage, and Javadoc generates API
documentation. Packaging produces the domain JAR. `verify.ps1` is a shortcut to
this Maven workflow, with no separate test or compilation logic.

Maven 3.9.9 emits JDK 26 warnings from its bundled Jansi, Guava, and Sisu
libraries. These occurred during a successful build; they are separate from Java
compiler and JavaDoc warnings, which fail this project's verification. The build
retains its pinned Maven version without suppressing these messages.

Reference documentation: [Maven Wrapper](https://maven.apache.org/tools/wrapper/),
[JUnit](https://docs.junit.org/6.1.3/overview.html), and
[JaCoCo releases](https://www.jacoco.org/jacoco/trunk/doc/changes.html).

## AI Assistance Log

OpenAI Codex assisted with the grid design discussion and generated the initial
`Grid`, `Position`, dependency-free tests, verification script, and accompanying
documentation. It also recorded confirmed behavior separately from proposals,
generated the Apache Maven wrapper, configured the Maven build, and migrated the
checks to JUnit in separate grid and position test files.
Further assistance added configurable terrain limits, the hard cap of `100`,
value equality, matching hashes, and focused contract tests.

Verification performed by Codex: the initial dependency-free checks passed on
JDK 25. The Maven build subsequently passed on Eclipse Temurin 26.0.2.1 with
Maven 3.9.9: the JUnit suite passed, coverage was generated, and strict JavaDoc
checks passed. This does not establish algorithm correctness or web integration.
Team review and each student's independent explanation remain to be completed.
Extend this log as further assistance or verification occurs.
