package pathfinder.search;

import pathfinder.grid.Grid;

/**
 * Depth-first search (DFS): follows one route as far as it can, using a
 * last-in, first-out stack, and backs up only at dead ends.
 *
 * <p>It always finds a path when one exists, but that path is usually not the
 * shortest or the cheapest. Neighbors are pushed in the grid's order (up, right,
 * down, left), so they are tried in the reverse order: left, down, right, up.
 */
public final class DepthFirstSearch extends SearchAlgorithm {
    /** Creates a reusable depth-first search. */
    public DepthFirstSearch() {
    }

    @Override
    protected Frontier createFrontier(Grid grid, int target) {
        return new StackFrontier(grid.size());
    }

    @Override
    protected boolean shouldUpdate(long candidateCost, long knownCost) {
        // Always take the newest route: its cell goes on top of the stack and is
        // expanded next, which is what sends the search deeper.
        return true;
    }
}
