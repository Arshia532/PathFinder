package pathfinder.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import pathfinder.grid.Grid;
import pathfinder.grid.Position;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pathfinder.search.SearchFixtures.assertValidPath;
import static pathfinder.search.SearchFixtures.lastCell;
import static pathfinder.search.SearchFixtures.parse;
import static pathfinder.search.SearchFixtures.randomOpenCell;
import static pathfinder.search.SearchFixtures.randomWeighted;

/** Rules that every algorithm inherits from the shared search loop. */
class SearchAlgorithmTest {
    static Stream<Named<SearchAlgorithm>> algorithms() {
        return Stream.of(
                Named.of("BFS", new BreadthFirstSearch()),
                Named.of("DFS", new DepthFirstSearch()),
                Named.of("Dijkstra", new DijkstraSearch()),
                Named.of("A*", new AStarSearch()));
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void rejectsWallEndpointsBeforeCheckingIdentity(SearchAlgorithm algorithm) {
        Grid grid = parse(".#.");
        Position open = new Position(0, 0);
        Position wall = new Position(0, 1);
        assertThrows(IllegalArgumentException.class, () -> algorithm.search(grid, wall, open));
        assertThrows(IllegalArgumentException.class, () -> algorithm.search(grid, open, wall));
        assertThrows(IllegalArgumentException.class, () -> algorithm.search(grid, wall, wall));
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void wallEndpointMessagesSayWhichEndpointIsBlocked(SearchAlgorithm algorithm) {
        Grid grid = parse(".#.");
        Position open = new Position(0, 0);
        Position wall = new Position(0, 1);
        assertNotEquals(SearchAlgorithm.START_BLOCKED_MESSAGE, SearchAlgorithm.DESTINATION_BLOCKED_MESSAGE);
        assertEquals(SearchAlgorithm.START_BLOCKED_MESSAGE,
                assertThrows(IllegalArgumentException.class,
                        () -> algorithm.search(grid, wall, open)).getMessage());
        assertEquals(SearchAlgorithm.DESTINATION_BLOCKED_MESSAGE,
                assertThrows(IllegalArgumentException.class,
                        () -> algorithm.search(grid, open, wall)).getMessage());
        assertEquals(SearchAlgorithm.START_BLOCKED_MESSAGE,
                assertThrows(IllegalArgumentException.class,
                        () -> algorithm.search(grid, wall, wall)).getMessage(),
                "The start is reported when both endpoints are walls");
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void rejectsMissingOrOutOfBoundsArguments(SearchAlgorithm algorithm) {
        Grid grid = new Grid(2, 2);
        Position inside = new Position(0, 0);
        Position outside = new Position(0, 2);
        assertThrows(IndexOutOfBoundsException.class, () -> algorithm.search(grid, outside, inside));
        assertThrows(IndexOutOfBoundsException.class, () -> algorithm.search(grid, inside, outside));
        assertThrows(NullPointerException.class, () -> algorithm.search(null, inside, inside));
        assertThrows(NullPointerException.class, () -> algorithm.search(grid, null, inside));
        assertThrows(NullPointerException.class, () -> algorithm.search(grid, inside, null));
        assertThrows(NullPointerException.class, () -> algorithm.search(grid, inside, inside, null));
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void identicalEndpointsSucceedWithZeroMovesAndCost(SearchAlgorithm algorithm) {
        for (Grid grid : List.of(new Grid(1, 1), parse("...", ".9.", "..."))) {
            Position cell = grid.positionAt(grid.size() / 2);
            SearchResult result = algorithm.search(grid, cell, cell);
            assertTrue(result.found());
            assertEquals(List.of(cell), result.path());
            assertEquals(0, result.moves());
            assertEquals(0, result.cost());
            assertEquals(1, result.expandedCount());
        }
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void unreachableGoalIsANormalResult(SearchAlgorithm algorithm) {
        Grid grid = parse(
                "..#..",
                "..#..");
        SearchResult result = algorithm.search(grid, new Position(0, 0), new Position(1, 4));
        assertFalse(result.found());
        assertTrue(result.path().isEmpty());
        assertEquals(0, result.moves());
        assertEquals(0, result.cost());
        assertEquals(4, result.expandedCount(), "Every cell on the start's side is explored");
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void observerSeesEveryStep(SearchAlgorithm algorithm) {
        Grid grid = randomWeighted(7L, 8, 8, 0.2);
        Position start = new Position(0, 0);
        Position goal = lastCell(grid);
        int[] discovered = {0};
        int[] expanded = {0};
        int[] lastExpanded = {-1};
        SearchResult result = algorithm.search(grid, start, goal, new SearchObserver() {
            @Override
            public void cellDiscovered(int cell) {
                discovered[0]++;
            }

            @Override
            public void cellExpanded(int cell) {
                expanded[0]++;
                lastExpanded[0] = cell;
            }
        });
        assertTrue(result.found());
        assertEquals(result.expandedCount(), expanded[0]);
        assertTrue(discovered[0] >= expanded[0], "Each expanded cell was discovered first");
        assertEquals(grid.indexOf(goal), lastExpanded[0], "The goal is expanded last");
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void observerSeesEachCellDiscoveredOnceThenExpandedAtMostOnce(SearchAlgorithm algorithm) {
        for (long seed = 0; seed < 100; seed++) {
            Grid grid = randomWeighted(seed, 9, 11, 0.25);
            Random random = new Random(seed);
            Position start = randomOpenCell(grid, random);
            Position goal = randomOpenCell(grid, random);
            List<Integer> discovered = new ArrayList<>();
            List<Integer> expanded = new ArrayList<>();
            SearchResult result = algorithm.search(grid, start, goal, new SearchObserver() {
                @Override
                public void cellDiscovered(int cell) {
                    assertFalse(grid.isBlocked(cell), "Walls are never discovered");
                    assertFalse(discovered.contains(cell), "Cell " + cell + " discovered twice");
                    discovered.add(cell);
                }

                @Override
                public void cellExpanded(int cell) {
                    assertTrue(discovered.contains(cell), "Cell " + cell + " expanded before discovery");
                    assertFalse(expanded.contains(cell), "Cell " + cell + " expanded twice");
                    expanded.add(cell);
                }
            });
            assertEquals(grid.indexOf(start), discovered.getFirst(), "The start is discovered first");
            assertEquals(grid.indexOf(start), expanded.getFirst(), "The start is expanded first");
            assertEquals(result.expandedCount(), expanded.size());
            if (result.found()) {
                assertEquals(grid.indexOf(goal), expanded.getLast(), "The goal is expanded last");
            } else {
                assertEquals(discovered.size(), expanded.size(),
                        "A failed search expands everything it discovered");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void handlesALargeOpenGrid(SearchAlgorithm algorithm) {
        // 40,000 cells: checks that the frontiers grow correctly and nothing recurses.
        Grid grid = new Grid(200, 200);
        Position start = new Position(0, 0);
        Position goal = lastCell(grid);
        SearchResult result = algorithm.search(grid, start, goal);
        assertValidPath(grid, start, goal, result);
        assertTrue(result.moves() >= 398, "No path can beat the Manhattan distance");
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void reusedInstanceGivesIdenticalResults(SearchAlgorithm algorithm) {
        Grid grid = randomWeighted(3L, 10, 12, 0.25);
        Position start = new Position(0, 0);
        Position goal = lastCell(grid);
        SearchResult first = algorithm.search(grid, start, goal);
        algorithm.search(grid, goal, start);
        assertEquals(first, algorithm.search(grid, start, goal));
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void pathsAreValidOnRandomWeightedMaps(SearchAlgorithm algorithm) {
        int foundCount = 0;
        for (long seed = 0; seed < 200; seed++) {
            Grid grid = randomWeighted(seed, 12, 15, 0.3);
            Position start = new Position(0, 0);
            Position goal = lastCell(grid);
            SearchResult result = algorithm.search(grid, start, goal);
            assertTrue(result.expandedCount() <= grid.size(), "Each cell is expanded at most once");
            if (result.found()) {
                assertValidPath(grid, start, goal, result);
                foundCount++;
            }
        }
        assertTrue(foundCount > 0, "Some random maps must be solvable for this test to mean anything");
    }
}
