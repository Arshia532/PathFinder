package pathfinder.search;

import pathfinder.grid.Grid;

/**
 * A* search: expands cells in order of lowest {@code f(n) = g(n) + h(n)},
 * adding the known path cost from the start {@code g(n)} to a heuristic lower
 * bound on the remaining cost to the goal {@code h(n)}.
 *
 * <p>The heuristic is the Manhattan distance {@code |r1 - r2| + |c1 - c2|}
 * multiplied by {@code 1}, the minimum terrain cost allowed by {@link Grid}.
 * Because orthogonal movement takes at least that many steps and each entered
 * cell costs at least {@code 1}, the heuristic never overestimates the true
 * remaining cost (admissible) and changes by at most {@code 1} per move
 * (consistent). Every cell's first expansion is therefore already optimal, and
 * A* always finds a lowest-cost path while typically expanding far fewer cells
 * than {@link DijkstraSearch}.
 */
public final class AStarSearch extends SearchAlgorithm {
    /** Creates a reusable A* search. */
    public AStarSearch() {
    }

    @Override
    protected Frontier createFrontier(Grid grid, int target) {
        int columns = grid.columns();
        int targetRow = target / columns;
        int targetColumn = target % columns;
        return new PriorityFrontier(grid.size(), cell ->
                (long) Math.abs(cell / columns - targetRow) + Math.abs(cell % columns - targetColumn));
    }

    @Override
    protected boolean shouldUpdate(long candidateCost, long knownCost) {
        // Replace the recorded route only when a strictly cheaper one is found.
        return candidateCost < knownCost;
    }
}
