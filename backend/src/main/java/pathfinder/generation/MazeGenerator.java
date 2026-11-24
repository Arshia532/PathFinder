package pathfinder.generation;

import java.util.Arrays;
import java.util.Random;

import pathfinder.grid.Grid;
import pathfinder.grid.Position;

/** Generates repeatable mazes with one route between any two open cells. */
public final class MazeGenerator {
    private static final int[] ROW_STEP = {-2, 0, 2, 0};
    private static final int[] COLUMN_STEP = {0, 2, 0, -2};

    /** Creates a stateless generator. */
    public MazeGenerator() {
    }

    /**
     * Carves one-cell corridors with a seeded depth-first backtracker.
     * The top and left borders are always walls. The bottom and right borders
     * are walls when that dimension is odd; when it is even, corridors run to
     * that edge instead, so no side is left with a wall two cells thick.
     * The same arguments always produce the same maze.
     *
     * @param rowCount number of rows, at least three
     * @param columnCount number of columns, at least three
     * @param seed starting value for the random sequence
     * @return an immutable grid and the suggested starting and destination cells
     * @throws IllegalArgumentException if either dimension is less than three
     *         or the cell count exceeds {@link Integer#MAX_VALUE}
     */
    public Maze generate(int rowCount, int columnCount, long seed) {
        if (rowCount < 3 || columnCount < 3) {
            throw new IllegalArgumentException("A maze needs at least three rows and three columns.");
        }
        long cellCount = (long) rowCount * columnCount;
        if (cellCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Grid has too many cells for array indexing.");
        }

        // Rooms sit on odd rows and columns. An even dimension has one more
        // odd index at its far edge, which becomes the last room.
        int roomRows = rowCount / 2;
        int roomColumns = columnCount / 2;
        int lastRoomRow = 2 * roomRows - 1;
        int lastRoomColumn = 2 * roomColumns - 1;
        boolean[] walls = new boolean[(int) cellCount];
        Arrays.fill(walls, true);
        int[] stack = new int[roomRows * roomColumns];
        int[] candidates = new int[Grid.MAX_NEIGHBORS];
        Random random = new Random(seed);

        int startIndex = columnCount + 1;
        walls[startIndex] = false;
        stack[0] = startIndex;
        int depth = 1;
        while (depth > 0) {
            int current = stack[depth - 1];
            int row = current / columnCount;
            int column = current % columnCount;
            int candidateCount = 0;
            for (int direction = 0; direction < ROW_STEP.length; direction++) {
                int nextRow = row + ROW_STEP[direction];
                int nextColumn = column + COLUMN_STEP[direction];
                if (nextRow >= 1 && nextRow <= lastRoomRow
                        && nextColumn >= 1 && nextColumn <= lastRoomColumn) {
                    int candidate = nextRow * columnCount + nextColumn;
                    if (walls[candidate]) {
                        candidates[candidateCount++] = candidate;
                    }
                }
            }
            if (candidateCount == 0) {
                depth--;
            } else {
                int next = candidates[random.nextInt(candidateCount)];
                // Open the wall exactly halfway between the two rooms.
                walls[current + (next - current) / 2] = false;
                walls[next] = false;
                stack[depth++] = next;
            }
        }

        int[] costs = new int[walls.length];
        Arrays.fill(costs, 1);
        Grid grid = new Grid(rowCount, columnCount, walls, costs);
        return new Maze(grid, new Position(1, 1), new Position(lastRoomRow, lastRoomColumn));
    }

    /**
     * A generated grid with suggested markers in opposite corners of the
     * corridors. The markers coincide for a maze with one open cell.
     *
     * @param grid immutable grid of walls and corridors
     * @param start suggested starting cell
     * @param destination suggested destination cell
     */
    public record Maze(Grid grid, Position start, Position destination) {
    }
}
