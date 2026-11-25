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
import static pathfinder.search.SearchFixtures.referenceCosts;

class AStarSearchTest {
    private final SearchAlgorithm astar = new AStarSearch();

    @Test
    void marchesStraightToTheGoalOnAnOpenGrid() {
        SearchResult small = astar.search(new Grid(3, 3), new Position(0, 0), new Position(2, 2));
        assertEquals(List.of(
                new Position(0, 0), new Position(0, 1), new Position(0, 2),
                new Position(1, 2), new Position(2, 2)), small.path());
        assertEquals(5, small.expandedCount(), "Only the five path cells are expanded");

        Grid large = new Grid(20, 20);
        SearchResult direct = astar.search(large, new Position(0, 0), lastCell(large));
        assertEquals(38, direct.moves());
        assertEquals(39, direct.expandedCount(), "Expands 39 cells out of 400");
    }

    @Test
    void detoursAroundExpensiveTerrainForLowerTotalCost() {
        Grid grid = parse(
                ".9.",
                "...");
        SearchResult result = astar.search(grid, new Position(0, 0), new Position(0, 2));
        assertEquals(List.of(
                new Position(0, 0), new Position(1, 0), new Position(1, 1),
                new Position(1, 2), new Position(0, 2)), result.path());
        assertEquals(4, result.cost());
    }

    @Test
    void replacesADiscoveredRouteAndSkipsOutdatedFrontierCopy() {
        // From (1,0) toward (1,3), (1,1) [g=3, h=2, f=5] pops before (0,0) [g=1, h=4, f=5]
        // and discovers (0,1) at g=4, f=7. Then (0,0) pops and relaxes (0,1) to g=2, f=5.
        // Because (0,3) costs 5 (pushing f to 9), the outdated f=7 entry for (0,1) is also
        // popped before the goal and skipped by lazy deletion.
        Grid relaxationMap = parse(
                "...5",
                ".3#.");
        int[] discoveries = {0};
        int[] expansions = {0};
        SearchResult result = astar.search(
                relaxationMap, new Position(1, 0), new Position(1, 3), new SearchObserver() {
                    @Override
                    public void cellDiscovered(int cell) {
                        if (cell == 1) {
                            discoveries[0]++;
                        }
                    }

                    @Override
                    public void cellExpanded(int cell) {
                        if (cell == 1) {
                            expansions[0]++;
                        }
                    }
                });
        assertTrue(result.found());
        assertEquals(1, discoveries[0], "Relaxed cell (0,1) is discovered only once");
        assertEquals(1, expansions[0], "Outdated copy of (0,1) is skipped on pop");
        assertEquals(9, result.cost(), "Uses the relaxed g=2 route through (0,0)->(0,1)");
    }

    @Test
    void findsOptimalCostWhileExpandingNoMoreCellsThanDijkstra() {
        SearchAlgorithm dijkstra = new DijkstraSearch();
        int foundCount = 0;
        long totalAStarExpanded = 0;
        long totalDijkstraExpanded = 0;

        for (long seed = 0; seed < 300; seed++) {
            Grid grid = randomWeighted(seed, 11, 13, 0.3, Grid.MAX_TERRAIN_COST);
            Random random = new Random(seed);
            Position start = randomOpenCell(grid, random);
            Position goal = randomOpenCell(grid, random);
            long expectedCost = referenceCosts(grid, start)[grid.indexOf(goal)];

            SearchResult astarResult = astar.search(grid, start, goal);
            SearchResult dijkstraResult = dijkstra.search(grid, start, goal);

            assertEquals(expectedCost >= 0, astarResult.found(), "Reachability on seed " + seed);
            assertEquals(dijkstraResult.found(), astarResult.found());
            assertTrue(astarResult.expandedCount() <= dijkstraResult.expandedCount(),
                    "A* should never expand more cells than Dijkstra on seed " + seed);

            totalAStarExpanded += astarResult.expandedCount();
            totalDijkstraExpanded += dijkstraResult.expandedCount();

            if (astarResult.found()) {
                assertEquals(expectedCost, astarResult.cost(), "Lowest cost on seed " + seed);
                assertEquals(dijkstraResult.cost(), astarResult.cost());
                assertValidPath(grid, start, goal, astarResult);
                foundCount++;
            }
        }
        assertTrue(foundCount > 0, "Some random maps must be solvable for this test to mean anything");
        assertTrue(totalAStarExpanded < totalDijkstraExpanded,
                "Across random maps A* expands fewer cells overall than Dijkstra");
    }
}
