package pathfinder.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import pathfinder.grid.Grid;
import pathfinder.grid.Position;

/**
 * The base class of every grid search. It contains the single search loop that
 * all algorithms share; each subclass only answers two questions:
 *
 * <ol>
 *   <li>{@link #createFrontier(Grid, int) Which frontier?} The frontier's pop
 *       order decides which discovered cell is expanded next.</li>
 *   <li>{@link #shouldUpdate(long, long) Keep a newly found route?} When a
 *       cell is reached again by a different route, should that route replace
 *       the one already recorded?</li>
 * </ol>
 *
 * <p>This is the Template Method pattern: {@link #search} is {@code final}, so
 * the loop and its rules are identical for every algorithm, while the two
 * {@code protected abstract} methods are the steps subclasses fill in.
 *
 * <p>Endpoint rules: endpoints outside the grid or on walls are rejected. A
 * start on a wall and a destination on a wall have separate messages; when
 * both are walls, the start is reported.
 * Identical traversable endpoints succeed at once with zero moves and zero cost.
 * An unreachable goal produces a result whose {@link SearchResult#found()} is
 * {@code false}.
 *
 * <p>Instances hold no state between searches, so one instance can be reused
 * and shared between threads. Each search takes time and memory proportional
 * to the number of grid cells.
 */
public abstract class SearchAlgorithm {
    /** The cost recorded for a cell that no route has reached yet. */
    protected static final long UNREACHED = Long.MAX_VALUE;

    /** The message of the exception thrown when the start cell is a wall. */
    public static final String START_BLOCKED_MESSAGE =
            "The start cell is a wall. Choose a traversable start cell.";

    /** The message of the exception thrown when the destination cell is a wall. */
    public static final String DESTINATION_BLOCKED_MESSAGE =
            "The destination cell is a wall. Choose a traversable destination cell.";

    /** The parent of the start cell, where every path begins. */
    private static final int NO_PARENT = -1;

    /** Creates an algorithm; called by subclass constructors. */
    protected SearchAlgorithm() {
    }

    /**
     * Searches for a path from {@code start} to {@code goal}.
     *
     * @param grid the map to search
     * @param start the starting cell
     * @param goal the destination cell
     * @return the path found, or an unreachable result
     * @throws NullPointerException if any argument is null
     * @throws IndexOutOfBoundsException if an endpoint is outside the grid
     * @throws IllegalArgumentException if the start is a wall (with
     *         {@link #START_BLOCKED_MESSAGE}) or the destination is a wall
     *         (with {@link #DESTINATION_BLOCKED_MESSAGE})
     */
    public final SearchResult search(Grid grid, Position start, Position goal) {
        return search(grid, start, goal, SearchObserver.NONE);
    }

    /**
     * Searches for a path from {@code start} to {@code goal}, reporting each
     * cell to {@code observer} when it is first discovered and when it is
     * expanded.
     *
     * @param grid the map to search
     * @param start the starting cell
     * @param goal the destination cell
     * @param observer receives each search step
     * @return the path found, or an unreachable result
     * @throws NullPointerException if any argument is null
     * @throws IndexOutOfBoundsException if an endpoint is outside the grid
     * @throws IllegalArgumentException if the start is a wall (with
     *         {@link #START_BLOCKED_MESSAGE}) or the destination is a wall
     *         (with {@link #DESTINATION_BLOCKED_MESSAGE})
     */
    public final SearchResult search(Grid grid, Position start, Position goal,
                                     SearchObserver observer) {
        Objects.requireNonNull(grid, "grid");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(goal, "goal");
        Objects.requireNonNull(observer, "observer");
        int source = grid.indexOf(start);
        int target = grid.indexOf(goal);
        // The start is checked first, so it is the one reported when both are walls.
        if (grid.isBlocked(source)) {
            throw new IllegalArgumentException(START_BLOCKED_MESSAGE);
        }
        if (grid.isBlocked(target)) {
            throw new IllegalArgumentException(DESTINATION_BLOCKED_MESSAGE);
        }

        // Per-search state, indexed by cell. parent[] is only read for reached cells.
        int cellCount = grid.size();
        int[] parent = new int[cellCount];
        long[] cost = new long[cellCount];
        Arrays.fill(cost, UNREACHED);
        boolean[] expanded = new boolean[cellCount];
        int[] neighbors = new int[Grid.MAX_NEIGHBORS];
        Frontier frontier = createFrontier(grid, target);

        parent[source] = NO_PARENT;
        cost[source] = 0;
        frontier.push(source, 0);
        observer.cellDiscovered(source);
        int expandedCount = 0;

        while (!frontier.isEmpty()) {
            int cell = frontier.pop();
            if (expanded[cell]) {
                continue; // An outdated copy of a cell that was already handled.
            }
            expanded[cell] = true;
            expandedCount++;
            observer.cellExpanded(cell);
            if (cell == target) {
                return new SearchResult(tracePath(grid, parent, target), cost[target], expandedCount);
            }

            int neighborCount = grid.neighbors(cell, neighbors);
            for (int i = 0; i < neighborCount; i++) {
                int next = neighbors[i];
                if (expanded[next]) {
                    continue; // An expanded cell's route is final.
                }
                long nextCost = cost[cell] + grid.terrainCost(next);
                long knownCost = cost[next];
                if (shouldUpdate(nextCost, knownCost)) {
                    parent[next] = cell;
                    cost[next] = nextCost;
                    frontier.push(next, nextCost);
                    if (knownCost == UNREACHED) {
                        // Report only the first route; later ones just replace it.
                        observer.cellDiscovered(next);
                    }
                }
            }
        }
        return SearchResult.unreachable(expandedCount);
    }

    /**
     * Creates the empty frontier for one search. Its pop order defines the
     * order in which cells are expanded.
     *
     * @param grid the map being searched, for sizing the frontier
     * @param target the goal cell's row-major index, for frontiers that
     *        prioritize cells close to the goal
     * @return a new, empty frontier
     */
    protected abstract Frontier createFrontier(Grid grid, int target);

    /**
     * Decides whether a newly found route to an unexpanded cell replaces the
     * route already recorded for it. Returning {@code true} records the new
     * parent and cost and pushes the cell onto the frontier again.
     *
     * @param candidateCost the total cost of the newly found route
     * @param knownCost the cost of the recorded route, or {@link #UNREACHED}
     *        if the cell has not been reached before
     * @return whether to record the new route
     */
    protected abstract boolean shouldUpdate(long candidateCost, long knownCost);

    /** Follows parent links back from the goal, then reverses them into start-to-goal order. */
    private static List<Position> tracePath(Grid grid, int[] parent, int target) {
        List<Position> path = new ArrayList<>();
        for (int cell = target; cell != NO_PARENT; cell = parent[cell]) {
            path.add(grid.positionAt(cell));
        }
        Collections.reverse(path);
        return path;
    }
}
