package pathfinder.generation;

import java.util.ArrayDeque;
import java.util.Queue;

import org.junit.jupiter.api.Test;

import pathfinder.grid.Grid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MazeGeneratorTest {
    private final MazeGenerator generator = new MazeGenerator();

    @Test
    void openCellsFormOneConnectedTreeAcrossOddEvenAndNarrowSizes() {
        int[][] sizes = {{3, 3}, {3, 10}, {10, 3}, {10, 10}, {20, 31}, {21, 30}, {21, 31}, {50, 50}};
        for (int[] size : sizes) {
            for (long seed : new long[] {0L, -42L, 485161065432132L}) {
                MazeGenerator.Maze maze = generator.generate(size[0], size[1], seed);
                assertEquals(size[0], maze.grid().rows());
                assertEquals(size[1], maze.grid().columns());
                assertConnectedTree(maze);
            }
        }
    }

    @Test
    void rejectsDimensionsThatCannotFitAMazeOrOverflowGridIndices() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(2, 10, 0L));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(10, 2, 0L));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(50_000, 50_000, 0L));
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
