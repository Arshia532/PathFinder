package pathfinder.search;

import java.util.NoSuchElementException;

/**
 * A first-in, first-out frontier: cells are returned in the order they were
 * added.
 *
 * <p>Cells are stored in a circular {@code int[]} (a ring buffer): when the
 * end of the array is reached, writing continues at the start, so popping never
 * shifts elements. Primitive storage avoids creating an {@code Integer} object
 * for every push. The array doubles in size when full.
 */
final class QueueFrontier implements Frontier {
    private int[] cells;
    private int head;
    private int size;

    /**
     * Creates an empty queue.
     *
     * @param initialCapacity the number of cells to reserve space for
     */
    QueueFrontier(int initialCapacity) {
        cells = new int[Math.max(1, initialCapacity)];
    }

    @Override
    public void push(int cell, long cost) {
        if (size == cells.length) {
            grow();
        }
        cells[(head + size) % cells.length] = cell;
        size++;
    }

    @Override
    public int pop() {
        if (size == 0) {
            throw new NoSuchElementException("Frontier is empty.");
        }
        int cell = cells[head];
        head = (head + 1) % cells.length;
        size--;
        return cell;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /** Copies the cells, oldest first, into an array twice as large. */
    private void grow() {
        int[] larger = new int[cells.length * 2];
        for (int i = 0; i < size; i++) {
            larger[i] = cells[(head + i) % cells.length];
        }
        cells = larger;
        head = 0;
    }
}
