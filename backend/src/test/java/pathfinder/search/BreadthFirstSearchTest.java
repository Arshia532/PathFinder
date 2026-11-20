package pathfinder.search;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import pathfinder.grid.Grid;
import pathfinder.grid.Position;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pathfinder.search.SearchFixtures.assertValidPath;
import static pathfinder.search.SearchFixtures.lastCell;
import static pathfinder.search.SearchFixtures.parse;
import static pathfinder.search.SearchFixtures.randomOpenCell;
import static pathfinder.search.SearchFixtures.randomWeighted;
import static pathfinder.search.SearchFixtures.referenceDistances;

class BreadthFirstSearchTest {
    private final SearchAlgorithm bfs = new BreadthFirstSearch();

    @Test
    void openGridMovesEqualManhattanDistance() {
        Grid grid = new Grid(5, 7);
        Position start = new Position(0, 0);
        for (int index = 0; index < grid.size(); index++) {
            Position goal = grid.positionAt(index);
            SearchResult result = bfs.search(grid, start, goal);
            assertEquals(goal.row() + goal.column(), result.moves(), "Moves to " + goal);
            assertEquals(result.moves(), result.cost(), "Every cell costs 1");
        }
    }

    @Test
    void findsFewestMovesAroundWalls() {
        Grid grid = parse(
                "..#..",
                "..#..",
                ".##.#",
                ".....",
                "##.#.");
        // Down the left side, across row 3, then up the right side: 10 moves.
        SearchResult result = bfs.search(grid, new Position(0, 0), new Position(0, 4));
        assertEquals(10, result.moves());
    }

    @Test
    void prefersFewerMovesOverCheaperTerrain() {
        Grid grid = parse(
                ".9.",
                "...");
        SearchResult result = bfs.search(grid, new Position(0, 0), new Position(0, 2));
        assertEquals(List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2)),
                result.path(), "Two expensive moves beat four cheap ones");
        assertEquals(10, result.cost(), "Entering 9 then 1");
    }

    @Test
    void movesMatchAnIndependentDistanceTableOnRandomMaps() {
        int foundCount = 0;
        for (long seed = 0; seed < 300; seed++) {
            Grid grid = randomWeighted(seed, 11, 13, 0.3);
            Random random = new Random(seed);
            Position start = randomOpenCell(grid, random);
            Position goal = randomOpenCell(grid, random);
            int expected = referenceDistances(grid, start)[grid.indexOf(goal)];
            SearchResult result = bfs.search(grid, start, goal);
            assertEquals(expected >= 0, result.found(), "Reachability on seed " + seed);
            if (result.found()) {
                assertEquals(expected, result.moves(), "Fewest moves on seed " + seed);
                assertValidPath(grid, start, goal, result);
                foundCount++;
            }
        }
        assertTrue(foundCount > 0, "Some random maps must be solvable for this test to mean anything");
    }

    @Test
    void expandsCellsInOrderOfDistanceFromTheStart() {
        Grid grid = randomWeighted(11L, 12, 12, 0.2);
        Position start = new Position(0, 0);
        int[] distance = referenceDistances(grid, start);
        int[] previous = {0};
        bfs.search(grid, start, lastCell(grid), new SearchObserver() {
            @Override
            public void cellExpanded(int cell) {
                assertTrue(distance[cell] >= previous[0], "Rings are expanded nearest first");
                previous[0] = distance[cell];
            }
        });
    }

    @Test
    void producesTheSameExactPathEveryTime() {
        // Neighbors are discovered up, right, down, left, so ties favor going right first.
        SearchResult result = bfs.search(new Grid(3, 3), new Position(0, 0), new Position(2, 2));
        assertEquals(List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2),
                new Position(1, 2), new Position(2, 2)), result.path());
        assertEquals(9, result.expandedCount(), "The goal is in the last ring, so every cell is expanded");
    }
}
