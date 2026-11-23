package pathfinder.search;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * A last-in, first-out frontier: the most recently added cell is returned
 * first.
 *
 * <p>Cells are stored in a primitive {@code int[]} that doubles in size when
 * full, which avoids creating an {@code Integer} object for every push.
 */
final class StackFrontier implements Frontier {
    private int[] cells;
    private int size;

    /**
     * Creates an empty stack.
     *
     * @param initialCapacity the number of cells to reserve space for
     */
    StackFrontier(int initialCapacity) {
        cells = new int[Math.max(1, initialCapacity)];
    }

    @Override
    public void push(int cell, long cost) {
        if (size == cells.length) {
            cells = Arrays.copyOf(cells, cells.length * 2);
        }
        cells[size++] = cell;
    }

    @Override
    public int pop() {
        if (size == 0) {
            throw new NoSuchElementException("Frontier is empty.");
        }
        return cells[--size];
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }
}
