package pathfinder.search;

/**
 * The cells that a search has discovered but not yet expanded.
 *
 * <p>The order in which {@link #pop()} hands cells back is what makes one
 * algorithm behave differently from another: a queue gives breadth-first order,
 * a stack gives depth-first order, and a priority queue gives cheapest-first
 * order. The same cell may be pushed more than once; the search loop skips
 * copies of cells it has already expanded.
 */
public interface Frontier {
    /**
     * Adds a discovered cell.
     *
     * @param cell the cell's row-major index
     * @param cost the total path cost from the start to the cell; order-based
     *        frontiers, such as queues and stacks, ignore it
     */
    void push(int cell, long cost);

    /**
     * Removes and returns the next cell to expand.
     *
     * @return the next cell's row-major index
     * @throws java.util.NoSuchElementException if the frontier is empty
     */
    int pop();

    /**
     * Tests whether any cells are waiting to be expanded.
     *
     * @return whether the frontier is empty
     */
    boolean isEmpty();
}
