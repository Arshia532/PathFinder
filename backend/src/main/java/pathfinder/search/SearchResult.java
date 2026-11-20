package pathfinder.search;

import java.util.List;

import pathfinder.grid.Position;

/**
 * The outcome of one search. An unreachable goal is a normal outcome, not an
 * error: it is represented by an empty path.
 *
 * @param path the cells from start to goal, both included, or an empty list
 *        when the goal cannot be reached
 * @param cost the sum of the terrain costs of every cell entered along the
 *        path (the start cell adds nothing); zero when there is no path
 * @param expandedCount how many cells the search expanded, shown in the UI as
 *        "nodes visited"
 */
public record SearchResult(List<Position> path, long cost, int expandedCount) {

    /**
     * Creates a result, storing an unmodifiable copy of the path.
     *
     * @param path the cells from start to goal, or an empty list
     * @param cost the nonnegative path cost, zero when the path is empty
     * @param expandedCount the nonnegative number of expanded cells
     * @throws NullPointerException if the path or any of its positions is null
     * @throws IllegalArgumentException if a count is negative or an empty path
     *         has a nonzero cost
     */
    public SearchResult {
        path = List.copyOf(path);
        if (cost < 0 || expandedCount < 0) {
            throw new IllegalArgumentException("Cost and expanded count must be nonnegative.");
        }
        if (path.isEmpty() && cost != 0) {
            throw new IllegalArgumentException("A result without a path must have zero cost.");
        }
    }

    /**
     * Creates the result of a search that ran out of cells before reaching the goal.
     *
     * @param expandedCount the nonnegative number of expanded cells
     * @return a result with an empty path and zero cost
     */
    public static SearchResult unreachable(int expandedCount) {
        return new SearchResult(List.of(), 0, expandedCount);
    }

    /**
     * Tests whether the goal was reached.
     *
     * @return whether the path is non-empty
     */
    public boolean found() {
        return !path.isEmpty();
    }

    /**
     * Returns the number of moves along the path, which is one less than the
     * number of cells. Identical start and goal cells take zero moves.
     *
     * @return the move count, or zero when no path was found
     */
    public int moves() {
        return found() ? path.size() - 1 : 0;
    }
}
