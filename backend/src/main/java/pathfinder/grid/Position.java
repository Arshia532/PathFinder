package pathfinder.grid;

/**
 * A zero-based location on a grid. Rows increase downward and columns increase
 * to the right. A position does not have to belong to a particular grid.
 *
 * @param row the zero-based row index, not the total number of rows
 * @param column the zero-based column index, not the total number of columns
 */
public record Position(int row, int column) {

    /**
     * Creates a position with nonnegative coordinates.
     *
     * @param row the zero-based row index
     * @param column the zero-based column index
     * @throws IllegalArgumentException if either coordinate is negative
     */
    public Position {
        if (row < 0 || column < 0) {
            throw new IllegalArgumentException("Row and column must be nonnegative.");
        }
    }
}
