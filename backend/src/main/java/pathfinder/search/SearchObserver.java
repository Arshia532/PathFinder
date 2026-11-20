package pathfinder.search;

/**
 * Receives each step of a running search, for example to record playback
 * frames or count statistics. Both methods do nothing unless overridden, so an
 * observer only implements the events it needs.
 *
 * <p>Cells are reported as row-major indices to keep the search loop cheap;
 * convert them with {@link pathfinder.grid.Grid#positionAt(int)} when needed.
 * Each cell is discovered at most once and expanded at most once, and always
 * discovered before it is expanded. An algorithm may later replace the route to
 * a discovered cell (depth-first search does this often); that is not reported
 * again, so an observer can treat "discovered" as a one-time state change.
 */
public interface SearchObserver {
    /** An observer that ignores every event. */
    SearchObserver NONE = new SearchObserver() { };

    /**
     * Called the first time a cell is added to the frontier. The start cell is
     * the first cell discovered.
     *
     * @param cell the discovered cell's row-major index
     */
    default void cellDiscovered(int cell) {
    }

    /**
     * Called when a cell is taken from the frontier and its neighbors are about
     * to be examined. The last expanded cell of a successful search is the goal.
     *
     * @param cell the expanded cell's row-major index
     */
    default void cellExpanded(int cell) {
    }
}
