package pathfinder.generation;

import java.util.Arrays;
import java.util.Random;

import pathfinder.grid.Grid;

/** Generates repeatable random walls without selecting endpoints or terrain. */
public final class RandomGridGenerator {
    /** Creates a stateless generator. */
    public RandomGridGenerator() {
    }

    /**
     * Creates a grid using one random choice per cell in row-major order.
     * The same arguments reproduce the same walls. Wall probability is a chance
     * per cell, not an exact fraction of walls. A route is not guaranteed.
     * All cells, including walls, have terrain cost {@code 1}.
     *
     * @param rowCount positive number of rows
     * @param columnCount positive number of columns
     * @param seed starting value for the random sequence
     * @param wallProbability finite probability between zero and one, inclusive
     * @return a new immutable grid
     * @throws IllegalArgumentException if dimensions are nonpositive, the cell
     *         count exceeds {@link Integer#MAX_VALUE}, or probability is invalid
     */
    public Grid generate(int rowCount, int columnCount, long seed, double wallProbability) {
        if (rowCount <= 0 || columnCount <= 0) {
            throw new IllegalArgumentException("Rows and columns must be positive.");
        }
        // Cast before multiplying so large dimensions cannot overflow an int.
        long cellCount = (long) rowCount * columnCount;
        if (cellCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Grid has too many cells for array indexing.");
        }
        if (!Double.isFinite(wallProbability)
                || wallProbability < 0 || wallProbability > 1) {
            throw new IllegalArgumentException("Wall probability must be finite and between 0 and 1.");
        }

        boolean[] walls = new boolean[(int) cellCount];
        int[] costs = new int[(int) cellCount];
        Arrays.fill(costs, 1);
        Random random = new Random(seed);
        for (int index = 0; index < walls.length; index++) {
            walls[index] = random.nextDouble() < wallProbability;
        }
        return new Grid(rowCount, columnCount, walls, costs);
    }
}
