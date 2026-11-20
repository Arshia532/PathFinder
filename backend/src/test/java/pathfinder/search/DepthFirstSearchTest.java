package pathfinder.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import pathfinder.grid.Grid;
import pathfinder.grid.Position;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pathfinder.search.SearchFixtures.lastCell;
import static pathfinder.search.SearchFixtures.randomOpenCell;
import static pathfinder.search.SearchFixtures.randomWeighted;

class DepthFirstSearchTest {
    private final SearchAlgorithm dfs = new DepthFirstSearch();

    @Test
    void divesDownBeforeTurning() {
        // From the corner, down is pushed after right, so it comes off the stack first.
        SearchResult result = dfs.search(new Grid(3, 3), new Position(0, 0), new Position(2, 2));
        assertEquals(List.of(new Position(0, 0), new Position(1, 0), new Position(2, 0),
                new Position(2, 1), new Position(2, 2)), result.path());
        assertEquals(5, result.expandedCount(), "Only the cells on the dive are expanded");
    }

    @Test
    void pathIsNotNecessarilyShortest() {
        // The goal is one step right, but DFS goes down first and loops around to it.
        SearchResult result = dfs.search(new Grid(2, 2), new Position(0, 0), new Position(0, 1));
        assertEquals(List.of(new Position(0, 0), new Position(1, 0), new Position(1, 1),
                new Position(0, 1)), result.path());
        assertEquals(3, result.moves());
    }

    @Test
    void matchesATextbookRecursiveDepthFirstSearch() {
        for (long seed = 0; seed < 300; seed++) {
            Grid grid = randomWeighted(seed, 9, 12, 0.25);
            Random random = new Random(seed);
            Position start = randomOpenCell(grid, random);
            Position goal = randomOpenCell(grid, random);

            List<Integer> expectedOrder = new ArrayList<>();
            List<Position> expectedPath = new ArrayList<>();
            boolean expectedFound = recursiveSearch(grid, grid.indexOf(start), grid.indexOf(goal),
                    new boolean[grid.size()], expectedOrder, expectedPath);

            List<Integer> order = new ArrayList<>();
            SearchResult result = dfs.search(grid, start, goal, new SearchObserver() {
                @Override
                public void cellExpanded(int cell) {
                    order.add(cell);
                }
            });
            assertEquals(expectedFound, result.found(), "Reachability on seed " + seed);
            assertEquals(expectedOrder, order, "Expansion order on seed " + seed);
            assertEquals(expectedFound ? expectedPath : List.of(), result.path(), "Path on seed " + seed);
        }
    }

    /**
     * The classic recursive DFS, written independently of the search package:
     * visit a cell, then try each unvisited neighbor in turn. It tries the
     * grid's neighbor order backwards (left, down, right, up) because a stack
     * hands back the last neighbor pushed first.
     */
    private static boolean recursiveSearch(Grid grid, int cell, int goal, boolean[] visited,
                                           List<Integer> order, List<Position> path) {
        visited[cell] = true;
        order.add(cell);
        path.add(grid.positionAt(cell));
        if (cell == goal) {
            return true;
        }
        int[] neighbors = new int[Grid.MAX_NEIGHBORS];
        int count = grid.neighbors(cell, neighbors);
        for (int i = count - 1; i >= 0; i--) {
            if (!visited[neighbors[i]]
                    && recursiveSearch(grid, neighbors[i], goal, visited, order, path)) {
                return true;
            }
        }
        path.removeLast();
        return false;
    }

    @Test
    void findsAPathExactlyWhenBreadthFirstSearchDoes() {
        SearchAlgorithm bfs = new BreadthFirstSearch();
        for (long seed = 0; seed < 100; seed++) {
            Grid grid = randomWeighted(seed, 10, 10, 0.35);
            Position start = new Position(0, 0);
            Position goal = lastCell(grid);
            SearchResult deep = dfs.search(grid, start, goal);
            SearchResult broad = bfs.search(grid, start, goal);
            assertEquals(broad.found(), deep.found(), "Reachability on seed " + seed);
            assertTrue(deep.moves() >= broad.moves(), "BFS has the fewest moves on seed " + seed);
        }
    }
}
