package pathfinder.search;

import pathfinder.grid.Grid;

/**
 * Dijkstra's algorithm: expands cells in order of increasing total terrain cost
 * from the start, using a min-heap priority queue.
 *
 * <p>Because every cell's terrain cost is positive, the first time a cell is
 * expanded its recorded cost is minimal, so the path found always has the
 * lowest possible total terrain cost. On a grid where every cell has the same
 * cost, equal-cost ties break in discovery order and the search behaves like
 * {@link BreadthFirstSearch}.
 */
public final class DijkstraSearch extends SearchAlgorithm {
    /** Creates a reusable Dijkstra search. */
    public DijkstraSearch() {
    }

    @Override
    protected Frontier createFrontier(Grid grid, int target) {
        return new PriorityFrontier(grid.size());
    }

    @Override
    protected boolean shouldUpdate(long candidateCost, long knownCost) {
        // Replace the recorded route only when a strictly cheaper one is found.
        return candidateCost < knownCost;
    }
}
