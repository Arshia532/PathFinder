package pathfinder.grid;

import java.util.Arrays;
import java.util.Objects;

/**
 * An immutable rectangular map with walls and positive terrain costs.
 *
 * <p>Cells use row-major indices:
 * {@code rowIndex * columnCount + columnIndex}. Row and column indices start at
 * zero; {@code columnCount} is the total number of columns in the grid.
 * Movement is orthogonal, and moving into a traversable cell costs that cell's
 * terrain cost. The starting cell adds no cost. Walls are independent of terrain costs.
 *
 * <p>This class stores map data only. Endpoints, search state, and playback
 * belong to the caller. Instances can be shared between independent searches.
 */
public final class Grid {
    /** The maximum number of neighbors under four-direction movement. */
    public static final int MAX_NEIGHBORS = 4;

    private final int rowCount;
    private final int columnCount;
    private final boolean[] blocked;
    private final int[] terrainCosts;

    /**
     * Creates an open grid where every cell has terrain cost {@code 1}.
     *
     * @param rowCount the positive total number of rows
     * @param columnCount the positive total number of columns
     * @throws IllegalArgumentException if dimensions are nonpositive or the
     *         number of cells exceeds {@link Integer#MAX_VALUE}
     */
    public Grid(int rowCount, int columnCount) {
        int cellCount = checkedCellCount(rowCount, columnCount);
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.blocked = new boolean[cellCount];
        this.terrainCosts = new int[cellCount];
        Arrays.fill(terrainCosts, 1);
    }

    /**
     * Creates a map from row-major arrays, copying them to prevent later changes.
     * Every cell needs a positive terrain cost, including walls, so removing a
     * wall in a future map can preserve its underlying terrain.
     *
     * @param rowCount the positive total number of rows
     * @param columnCount the positive total number of columns
     * @param blocked whether each cell is impassable
     * @param terrainCosts the positive entry cost of each cell
     * @throws NullPointerException if either array is null
     * @throws IllegalArgumentException if dimensions are invalid, either array
     *         has the wrong length, or any terrain cost is nonpositive
     */
    public Grid(int rowCount, int columnCount, boolean[] blocked, int[] terrainCosts) {
        int cellCount = checkedCellCount(rowCount, columnCount);
        Objects.requireNonNull(blocked, "blocked");
        Objects.requireNonNull(terrainCosts, "terrainCosts");
        if (blocked.length != cellCount || terrainCosts.length != cellCount) {
            throw new IllegalArgumentException("Map arrays must contain rowCount * columnCount cells.");
        }

        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.blocked = blocked.clone();
        this.terrainCosts = terrainCosts.clone();
        for (int cost : this.terrainCosts) {
            if (cost <= 0) {
                throw new IllegalArgumentException("Terrain costs must be positive.");
            }
        }
    }

    /**
     * Returns the number of rows.
     * @return the positive row count
     */
    public int rows() {
        return rowCount;
    }

    /**
     * Returns the number of columns.
     * @return the positive column count
     */
    public int columns() {
        return columnCount;
    }

    /**
     * Returns the total number of cells, including walls.
     * @return the cell count
     */
    public int size() {
        return blocked.length;
    }

    /**
     * Tests coordinates without throwing for negative or out-of-bounds values.
     * @param rowIndex the zero-based row index to test
     * @param columnIndex the zero-based column index to test
     * @return whether the coordinates are inside this grid
     */
    public boolean contains(int rowIndex, int columnIndex) {
        return rowIndex >= 0 && rowIndex < rowCount
                && columnIndex >= 0 && columnIndex < columnCount;
    }

    /**
     * Converts a position to its row-major cell index.
     * @param position the position to convert
     * @return an index between zero (inclusive) and {@link #size()} (exclusive)
     * @throws NullPointerException if the position is null
     * @throws IndexOutOfBoundsException if the position is outside this grid
     */
    public int indexOf(Position position) {
        Objects.requireNonNull(position, "position");
        int rowIndex = position.row();
        int columnIndex = position.column();
        if (!contains(rowIndex, columnIndex)) {
            throw new IndexOutOfBoundsException("Position is outside the grid: " + position);
        }
        return rowIndex * columnCount + columnIndex;
    }

    /**
     * Converts a cell index to a position.
     * @param index the row-major cell index
     * @return the cell's position
     * @throws IndexOutOfBoundsException if the index is outside this grid
     */
    public Position positionAt(int index) {
        Objects.checkIndex(index, size());
        return new Position(index / columnCount, index % columnCount);
    }

    /**
     * Tests whether a cell is a wall.
     * @param index the row-major cell index
     * @return whether the cell is impassable
     * @throws IndexOutOfBoundsException if the index is outside this grid
     */
    public boolean isBlocked(int index) {
        return blocked[Objects.checkIndex(index, size())];
    }

    /**
     * Returns a cell's terrain cost. A positive cost does not imply that the
     * cell is traversable; callers must also respect {@link #isBlocked(int)}.
     * @param index the row-major cell index
     * @return the positive terrain cost
     * @throws IndexOutOfBoundsException if the index is outside this grid
     */
    public int terrainCost(int index) {
        return terrainCosts[Objects.checkIndex(index, size())];
    }

    /**
     * Writes traversable neighbors into a reusable caller-owned array, in order:
     * up, right, down, left. A wall has no traversable neighbors.
     *
     * <p>Only entries before the returned count are valid; later entries are
     * unchanged. Reuse one buffer per search to avoid allocating on each visit.
     *
     * @param index the source cell's row-major index
     * @param destination a buffer with at least {@link #MAX_NEIGHBORS} entries
     * @return the number of neighbor indices written
     * @throws IndexOutOfBoundsException if the source index is outside this grid
     * @throws NullPointerException if the buffer is null
     * @throws IllegalArgumentException if the buffer has fewer than four entries
     */
    public int neighbors(int index, int[] destination) {
        Objects.checkIndex(index, size());
        Objects.requireNonNull(destination, "destination");
        if (destination.length < MAX_NEIGHBORS) {
            throw new IllegalArgumentException("Neighbor buffer must have at least four entries.");
        }
        if (blocked[index]) {
            return 0;
        }

        int count = 0;
        int columnIndex = index % columnCount;
        if (index >= columnCount && !blocked[index - columnCount]) {
            destination[count++] = index - columnCount;
        }
        if (columnIndex < columnCount - 1 && !blocked[index + 1]) {
            destination[count++] = index + 1;
        }
        // Subtraction avoids overflowing when checking the last row.
        if (index < size() - columnCount && !blocked[index + columnCount]) {
            destination[count++] = index + columnCount;
        }
        if (columnIndex > 0 && !blocked[index - 1]) {
            destination[count++] = index - 1;
        }
        return count;
    }

    private static int checkedCellCount(int rowCount, int columnCount) {
        if (rowCount <= 0 || columnCount <= 0) {
            throw new IllegalArgumentException("Rows and columns must be positive.");
        }
        long cellCount = (long) rowCount * columnCount;
        if (cellCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Grid has too many cells for array indexing.");
        }
        return (int) cellCount;
    }
}
