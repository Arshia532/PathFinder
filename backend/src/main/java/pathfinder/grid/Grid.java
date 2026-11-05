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
 * Terrain costs must be between {@code 1} and the configured maximum, which
 * cannot exceed {@link #MAX_TERRAIN_COST}. Equality compares dimensions, the
 * configured maximum, walls, and terrain costs by value.
 */
public final class Grid {
    /** The maximum number of neighbors under four-direction movement. */
    public static final int MAX_NEIGHBORS = 4;

    /** The hard upper bound for any configured limit or individual terrain cost. */
    public static final int MAX_TERRAIN_COST = 100;

    private final int rowCount;
    private final int columnCount;
    private final int maxTerrainCost;
    private final boolean[] blocked;
    private final int[] terrainCosts;

    /**
     * Creates an open grid with terrain cost {@code 1} and a maximum of
     * {@link #MAX_TERRAIN_COST}.
     *
     * @param rowCount the positive total number of rows
     * @param columnCount the positive total number of columns
     * @throws IllegalArgumentException if dimensions are nonpositive or the
     *         number of cells exceeds {@link Integer#MAX_VALUE}
     */
    public Grid(int rowCount, int columnCount) {
        this(rowCount, columnCount, MAX_TERRAIN_COST);
    }

    /**
     * Creates an open grid with terrain cost {@code 1} and a configured maximum.
     *
     * @param rowCount the positive total number of rows
     * @param columnCount the positive total number of columns
     * @param maxTerrainCost the inclusive terrain-cost limit, from 1 to 100
     * @throws IllegalArgumentException if dimensions are invalid, the number of
     *         cells exceeds {@link Integer#MAX_VALUE}, or the limit is outside 1-100
     */
    public Grid(int rowCount, int columnCount, int maxTerrainCost) {
        int cellCount = checkedCellCount(rowCount, columnCount);
        checkTerrainLimit(maxTerrainCost);
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.maxTerrainCost = maxTerrainCost;
        this.blocked = new boolean[cellCount];
        this.terrainCosts = new int[cellCount];
        Arrays.fill(terrainCosts, 1);
    }

    /**
     * Creates a map from row-major arrays with a maximum terrain cost of
     * {@link #MAX_TERRAIN_COST}. The arrays are copied to prevent later changes.
     *
     * @param rowCount the positive total number of rows
     * @param columnCount the positive total number of columns
     * @param blocked whether each cell is impassable
     * @param terrainCosts each cell's entry cost, from 1 to 100
     * @throws NullPointerException if either array is null
     * @throws IllegalArgumentException if dimensions are invalid, either array
     *         has the wrong length, or any terrain cost is outside 1-100
     */
    public Grid(int rowCount, int columnCount, boolean[] blocked, int[] terrainCosts) {
        this(rowCount, columnCount, blocked, terrainCosts, MAX_TERRAIN_COST);
    }

    /**
     * Creates a map with a configured terrain-cost limit, copying the row-major
     * arrays. Costs on walls must also satisfy the limit, so a future map can
     * preserve valid underlying terrain when removing a wall.
     *
     * @param rowCount the positive total number of rows
     * @param columnCount the positive total number of columns
     * @param blocked whether each cell is impassable
     * @param terrainCosts each cell's entry cost, from 1 to maxTerrainCost
     * @param maxTerrainCost the inclusive terrain-cost limit, from 1 to 100
     * @throws NullPointerException if either array is null
     * @throws IllegalArgumentException if dimensions or the limit are invalid,
     *         either array has the wrong length, or any cost is outside the limit
     */
    public Grid(int rowCount, int columnCount, boolean[] blocked, int[] terrainCosts,
                int maxTerrainCost) {
        int cellCount = checkedCellCount(rowCount, columnCount);
        checkTerrainLimit(maxTerrainCost);
        Objects.requireNonNull(blocked, "blocked");
        Objects.requireNonNull(terrainCosts, "terrainCosts");
        if (blocked.length != cellCount || terrainCosts.length != cellCount) {
            throw new IllegalArgumentException("Map arrays must contain rowCount * columnCount cells.");
        }

        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.maxTerrainCost = maxTerrainCost;
        this.blocked = blocked.clone();
        this.terrainCosts = terrainCosts.clone();
        for (int cost : this.terrainCosts) {
            if (cost < 1 || cost > maxTerrainCost) {
                throw new IllegalArgumentException(
                        "Terrain costs must be between 1 and " + maxTerrainCost + ".");
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
     * Returns the configured inclusive limit, not the largest cost present in the map.
     * @return the terrain-cost limit, from 1 to 100
     */
    public int maxTerrainCost() {
        return maxTerrainCost;
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

    /**
     * Compares grid values, including dimensions, the configured terrain limit,
     * walls, and all terrain costs. Distinct arrays with identical contents match.
     *
     * @param other the object to compare
     * @return whether both objects represent the same grid value
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Grid grid)) {
            return false;
        }
        return rowCount == grid.rowCount
                && columnCount == grid.columnCount
                && maxTerrainCost == grid.maxTerrainCost
                && Arrays.equals(blocked, grid.blocked)
                && Arrays.equals(terrainCosts, grid.terrainCosts);
    }

    /**
     * Returns a hash consistent with {@link #equals(Object)}. Computing it scans
     * the cell arrays; it is not a unique identifier for a grid.
     *
     * @return the hash of the grid's dimensions, terrain limit, walls, and costs
     */
    @Override
    public int hashCode() {
        int result = rowCount;
        result = 31 * result + columnCount;
        result = 31 * result + maxTerrainCost;
        result = 31 * result + Arrays.hashCode(blocked);
        return 31 * result + Arrays.hashCode(terrainCosts);
    }

    private static void checkTerrainLimit(int maxTerrainCost) {
        if (maxTerrainCost < 1 || maxTerrainCost > MAX_TERRAIN_COST) {
            throw new IllegalArgumentException(
                    "Maximum terrain cost must be between 1 and " + MAX_TERRAIN_COST + ".");
        }
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
