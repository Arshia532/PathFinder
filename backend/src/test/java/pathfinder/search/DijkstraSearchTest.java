package pathfinder.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import pathfinder.generation.RandomGridGenerator;
import pathfinder.grid.Grid;
import pathfinder.grid.Position;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pathfinder.search.SearchFixtures.assertValidPath;
import static pathfinder.search.SearchFixtures.lastCell;
import static pathfinder.search.SearchFixtures.parse;
import static pathfinder.search.SearchFixtures.randomOpenCell;
import static pathfinder.search.SearchFixtures.randomWeighted;
import static pathfinder.search.SearchFixtures.referenceCosts;

class DijkstraSearchTest {
    private final SearchAlgorithm dijkstra = new DijkstraSearch();

    @Test
    void detoursAroundExpensiveTerrainForLowerTotalCost() {
        Grid grid = parse(
                ".9.",
                "...");
        // Four cheap moves around row 1 cost 4, beating two moves through 9 (cost 10).
        SearchResult result = dijkstra.search(grid, new Position(0, 0), new Position(0, 2));
        assertEquals(List.of(
                new Position(0, 0), new Position(1, 0), new Position(1, 1),
                new Position(1, 2), new Position(0, 2)), result.path());
        assertEquals(4, result.moves());
        assertEquals(4, result.cost());
    }

    @Test
    void choosesCheaperMultiStepRouteOverDirectExpensiveStep() {
        Grid grid = parse(
                ".9#",
                "11#",
                "#2.");
        // Entering (0,1)=9 then (1,1)=1 costs 10; going through (1,0)=1 then (1,1)=1 costs 2.
        SearchResult result = dijkstra.search(grid, new Position(0, 0), new Position(2, 2));
        assertEquals(List.of(
                new Position(0, 0), new Position(1, 0), new Position(1, 1),
                new Position(2, 1), new Position(2, 2)), result.path());
        assertEquals(5, result.cost());
        assertValidPath(grid, new Position(0, 0), new Position(2, 2), result);
    }

    @Test
    void matchesBreadthFirstSearchOnUniformCostGrids() {
        SearchAlgorithm bfs = new BreadthFirstSearch();
        RandomGridGenerator generator = new RandomGridGenerator();
        for (long seed = 0; seed < 100; seed++) {
            Grid raw = generator.generate(10, 12, seed, 0.25);
            Random random = new Random(seed);
            if (raw.isBlocked(0)) {
                continue;
            }
            Position start = randomOpenCell(raw, random);
            Position goal = randomOpenCell(raw, random);

            List<Integer> bfsExpanded = new ArrayList<>();
            List<Integer> dijkstraExpanded = new ArrayList<>();
            SearchResult expected = bfs.search(raw, start, goal, new SearchObserver() {
                @Override
                public void cellExpanded(int cell) {
                    bfsExpanded.add(cell);
                }
            });
            SearchResult actual = dijkstra.search(raw, start, goal, new SearchObserver() {
                @Override
                public void cellExpanded(int cell) {
                    dijkstraExpanded.add(cell);
                }
            });
            assertEquals(expected, actual, "Result on uniform map seed " + seed);
            assertEquals(bfsExpanded, dijkstraExpanded, "Expansion order on uniform map seed " + seed);
        }
    }

    @Test
    void expandsCellsInNondecreasingOrderOfCost() {
        Grid grid = randomWeighted(19L, 12, 14, 0.2, Grid.MAX_TERRAIN_COST);
        Position start = new Position(0, 0);
        long[] optimalCosts = referenceCosts(grid, start);
        long[] previous = {0L};
        dijkstra.search(grid, start, lastCell(grid), new SearchObserver() {
            @Override
            public void cellExpanded(int cell) {
                assertTrue(optimalCosts[cell] >= previous[0],
                        "Expanded cell costs must never decrease");
                previous[0] = optimalCosts[cell];
            }
        });
    }

    @Test
    void costsMatchAnIndependentBellmanFordTableOnRandomWeightedMaps() {
        SearchAlgorithm bfs = new BreadthFirstSearch();
        int foundCount = 0;
        for (long seed = 0; seed < 300; seed++) {
            Grid grid = randomWeighted(seed, 11, 13, 0.3, Grid.MAX_TERRAIN_COST);
            Random random = new Random(seed);
            Position start = randomOpenCell(grid, random);
            Position goal = randomOpenCell(grid, random);
            long expectedCost = referenceCosts(grid, start)[grid.indexOf(goal)];

            SearchResult result = dijkstra.search(grid, start, goal);
            assertEquals(expectedCost >= 0, result.found(), "Reachability on seed " + seed);
            if (result.found()) {
                assertEquals(expectedCost, result.cost(), "Lowest cost on seed " + seed);
                assertValidPath(grid, start, goal, result);
                assertTrue(result.cost() <= bfs.search(grid, start, goal).cost(),
                        "Dijkstra never loses to BFS on cost");
                foundCount++;
            }
        }
        assertTrue(foundCount > 0, "Some random maps must be solvable for this test to mean anything");
    }
}
