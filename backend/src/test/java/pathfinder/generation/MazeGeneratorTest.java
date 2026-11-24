package pathfinder.generation;

import java.util.ArrayDeque;
import java.util.Queue;

import org.junit.jupiter.api.Test;

import pathfinder.grid.Grid;
import pathfinder.grid.Position;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MazeGeneratorTest {
    private static final int[][] SIZES = {
        {3, 3}, {3, 4}, {4, 3}, {4, 4}, {3, 10}, {10, 3}, {10, 10},
        {14, 20}, {20, 31}, {21, 30}, {21, 31}, {35, 50}, {50, 50}
    };
    private static final long[] SEEDS = {0L, -42L, 485161065432132L};

    private final MazeGenerator generator = new MazeGenerator();

    @Test
    void openCellsFormOneConnectedTreeAcrossOddEvenAndNarrowSizes() {
        for (int[] size : SIZES) {
            for (long seed : SEEDS) {
                MazeGenerator.Maze maze = generator.generate(size[0], size[1], seed);
                assertEquals(size[0], maze.grid().rows());
                assertEquals(size[1], maze.grid().columns());
                assertConnectedTree(maze);
            }
        }
    }

    @Test
    void sameArgumentsReproduceTheSameMaze() {
        MazeGenerator.Maze first = generator.generate(21, 30, 48213L);
        new MazeGenerator().generate(35, 50, 7L);
        MazeGenerator.Maze second = new MazeGenerator().generate(21, 30, 48213L);

        assertEquals(first, second);
    }

    @Test
    void differentSeedsGiveDifferentMazes() {
        Grid first = generator.generate(21, 30, 1L).grid();
        Grid second = generator.generate(21, 30, 2L).grid();

        assertNotEquals(first, second);
    }

    @Test
    void fixedSeedGivesAKnownMaze() {
        assertPattern(generator.generate(7, 9, 48213L).grid(),
                "#########",
                "#.#.....#",
                "#.#####.#",
                "#.....#.#",
                "#####.#.#",
                "#.......#",
                "#########");
        assertPattern(generator.generate(6, 8, 48213L).grid(),
                "########",
                "#.#.....",
                "#.#####.",
                "#.....#.",
                "#####.#.",
                "#.......");
    }

    @Test
    void bordersAreWallsOnOddSidesAndCorridorsReachTheEdgeOnEvenSides() {
        for (int[] size : SIZES) {
            int rows = size[0];
            int columns = size[1];
            Grid grid = generator.generate(rows, columns, 48213L).grid();
            for (int column = 0; column < columns; column++) {
                assertTrue(isWall(grid, 0, column), "top border");
            }
            for (int row = 0; row < rows; row++) {
                assertTrue(isWall(grid, row, 0), "left border");
            }
            boolean bottomOpen = false;
            for (int column = 0; column < columns; column++) {
                bottomOpen |= !isWall(grid, rows - 1, column);
            }
            boolean rightOpen = false;
            for (int row = 0; row < rows; row++) {
                rightOpen |= !isWall(grid, row, columns - 1);
            }
            assertEquals(rows % 2 == 0, bottomOpen, "bottom edge of " + rows + "x" + columns);
            assertEquals(columns % 2 == 0, rightOpen, "right edge of " + rows + "x" + columns);
        }
    }

    @Test
    void cellsOnEvenRowsAndEvenColumnsAreAlwaysWalls() {
        // These are the corners between rooms. Keeping them closed means no
        // corridor is ever two cells wide.
        for (int[] size : SIZES) {
            Grid grid = generator.generate(size[0], size[1], 48213L).grid();
            for (int row = 0; row < size[0]; row += 2) {
                for (int column = 0; column < size[1]; column += 2) {
                    assertTrue(isWall(grid, row, column), "corner (" + row + ", " + column + ")");
                }
            }
        }
    }

    @Test
    void markersSitInOppositeCornersOfTheCorridors() {
        MazeGenerator.Maze odd = generator.generate(21, 31, 48213L);
        assertEquals(new Position(1, 1), odd.start());
        assertEquals(new Position(19, 29), odd.destination());

        MazeGenerator.Maze even = generator.generate(14, 20, 48213L);
        assertEquals(new Position(1, 1), even.start());
        assertEquals(new Position(13, 19), even.destination());

        MazeGenerator.Maze single = generator.generate(3, 3, 48213L);
        assertEquals(single.start(), single.destination());
    }

    @Test
    void rejectsDimensionsThatCannotFitAMazeOrOverflowGridIndices() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(2, 10, 0L));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(10, 2, 0L));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(-3, 10, 0L));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(50_000, 50_000, 0L));
    }

    private static boolean isWall(Grid grid, int row, int column) {
        return grid.isBlocked(grid.indexOf(new Position(row, column)));
    }

    private static void assertPattern(Grid grid, String... rows) {
        assertEquals(rows.length, grid.rows());
        for (int row = 0; row < rows.length; row++) {
            StringBuilder actual = new StringBuilder();
            for (int column = 0; column < grid.columns(); column++) {
                actual.append(isWall(grid, row, column) ? '#' : '.');
            }
            assertEquals(rows[row], actual.toString(), "row " + row);
        }
    }

    private static void assertConnectedTree(MazeGenerator.Maze maze) {
        Grid grid = maze.grid();
        int start = grid.indexOf(maze.start());
        int end = grid.indexOf(maze.destination());
        assertFalse(grid.isBlocked(start));
        assertFalse(grid.isBlocked(end));
        int open = 0;
        for (int index = 0; index < grid.size(); index++) {
            assertEquals(1, grid.terrainCost(index));
            if (!grid.isBlocked(index)) {
                open++;
            }
        }

        boolean[] visited = new boolean[grid.size()];
        int[] neighbors = new int[Grid.MAX_NEIGHBORS];
        Queue<Integer> pending = new ArrayDeque<>();
        pending.add(start);
        visited[start] = true;
        int reached = 0;
        int degreeSum = 0;
        while (!pending.isEmpty()) {
            int current = pending.remove();
            reached++;
            int count = grid.neighbors(current, neighbors);
            degreeSum += count;
            for (int i = 0; i < count; i++) {
                int next = neighbors[i];
                if (!visited[next]) {
                    visited[next] = true;
                    pending.add(next);
                }
            }
        }
        assertTrue(visited[end]);
        assertEquals(open, reached, "Every corridor must be reachable.");
        assertEquals(2 * (open - 1), degreeSum, "The corridors must have no cycles.");
    }
}
