package pathfinder.search;

import pathfinder.grid.Grid;

/**
 * Breadth-first search (BFS): expands cells in rings of increasing distance
 * from the start, using a first-in, first-out queue. Every cell one move away is
 * expanded before any cell two moves away, and so on.
 *
 * <p>The path found has the fewest possible moves. Terrain costs do not affect
 * which path is chosen, but the result reports the terrain cost of that path.
 */
public final class BreadthFirstSearch extends SearchAlgorithm {
    /** Creates a reusable breadth-first search. */
    public BreadthFirstSearch() {
    }

    @Override
    protected Frontier createFrontier(Grid grid, int target) {
        // Each cell is queued at most once (see shouldUpdate), so this never grows.
        return new QueueFrontier(grid.size());
    }

    @Override
    protected boolean shouldUpdate(long candidateCost, long knownCost) {
        // Keep the first route found: the queue guarantees it has the fewest moves.
        return knownCost == UNREACHED;
    }
}
