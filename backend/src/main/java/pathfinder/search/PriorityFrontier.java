package pathfinder.search;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntToLongFunction;

/**
 * A min-heap priority frontier: returns the cell with the lowest estimated
 * total cost {@code f(n) = g(n) + h(n)} first.
 *
 * <p>For Dijkstra's algorithm {@code h(n)} is zero, so priority equals the
 * path cost {@code g(n)}. For A* {@code h(n)} estimates the remaining cost to
 * the goal. When two entries have the same priority, the one with the larger
 * known path cost {@code g(n)} (equivalently, the smaller remaining heuristic
 * {@code h(n)}) comes first; remaining ties break in insertion order so
 * behavior is completely deterministic.
 *
 * <p>Entries are stored in parallel primitive arrays ({@code int[]} and
 * {@code long[]}) arranged as a zero-based binary heap that doubles in size
 * when full, avoiding per-push object allocations.
 */
final class PriorityFrontier implements Frontier {
    private static final IntToLongFunction ZERO_HEURISTIC = cell -> 0L;

    private final IntToLongFunction heuristic;
    private int[] cells;
    private long[] priorities;
    private long[] costs;
    private long[] orders;
    private int size;
    private long nextOrder;

    /**
     * Creates an empty priority frontier ordered by path cost alone.
     *
     * @param initialCapacity the number of entries to reserve space for
     */
    PriorityFrontier(int initialCapacity) {
        this(initialCapacity, ZERO_HEURISTIC);
    }

    /**
     * Creates an empty priority frontier ordered by {@code cost + heuristic(cell)}.
     *
     * @param initialCapacity the number of entries to reserve space for
     * @param heuristic maps a cell's row-major index to its nonnegative
     *        estimated remaining cost to the goal
     * @throws NullPointerException if {@code heuristic} is null
     */
    PriorityFrontier(int initialCapacity, IntToLongFunction heuristic) {
        this.heuristic = Objects.requireNonNull(heuristic, "heuristic");
        int capacity = Math.max(1, initialCapacity);
        this.cells = new int[capacity];
        this.priorities = new long[capacity];
        this.costs = new long[capacity];
        this.orders = new long[capacity];
    }

    @Override
    public void push(int cell, long cost) {
        if (size == cells.length) {
            grow();
        }
        long priority = cost + heuristic.applyAsLong(cell);
        long order = nextOrder++;
        int index = size++;
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (!comesBefore(priority, cost, order,
                    priorities[parent], costs[parent], orders[parent])) {
                break;
            }
            copyEntry(parent, index);
            index = parent;
        }
        setEntry(index, cell, priority, cost, order);
    }

    @Override
    public int pop() {
        if (size == 0) {
            throw new NoSuchElementException("Frontier is empty.");
        }
        int bestCell = cells[0];
        size--;
        if (size > 0) {
            int cell = cells[size];
            long priority = priorities[size];
            long cost = costs[size];
            long order = orders[size];
            int index = 0;
            int half = size / 2;
            while (index < half) {
                int child = 2 * index + 1;
                int right = child + 1;
                if (right < size && comesBefore(
                        priorities[right], costs[right], orders[right],
                        priorities[child], costs[child], orders[child])) {
                    child = right;
                }
                if (!comesBefore(priorities[child], costs[child], orders[child],
                        priority, cost, order)) {
                    break;
                }
                copyEntry(child, index);
                index = child;
            }
            setEntry(index, cell, priority, cost, order);
        }
        return bestCell;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    private static boolean comesBefore(long priorityA, long costA, long orderA,
                                       long priorityB, long costB, long orderB) {
        if (priorityA != priorityB) {
            return priorityA < priorityB;
        }
        if (costA != costB) {
            return costA > costB;
        }
        return orderA < orderB;
    }

    private void copyEntry(int from, int to) {
        cells[to] = cells[from];
        priorities[to] = priorities[from];
        costs[to] = costs[from];
        orders[to] = orders[from];
    }

    private void setEntry(int index, int cell, long priority, long cost, long order) {
        cells[index] = cell;
        priorities[index] = priority;
        costs[index] = cost;
        orders[index] = order;
    }

    /** Doubles the backing arrays when the heap is full. */
    private void grow() {
        int newCapacity = cells.length * 2;
        cells = Arrays.copyOf(cells, newCapacity);
        priorities = Arrays.copyOf(priorities, newCapacity);
        costs = Arrays.copyOf(costs, newCapacity);
        orders = Arrays.copyOf(orders, newCapacity);
    }
}
