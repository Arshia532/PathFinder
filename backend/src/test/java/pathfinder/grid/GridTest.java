package pathfinder.grid;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridTest {
    @Test
    void rectangularCoordinates() {
        Grid grid = new Grid(3, 5);
        assertEquals(3, grid.rows());
        assertEquals(5, grid.columns());
        assertEquals(15, grid.size());
        for (int rowIndex = 0; rowIndex < grid.rows(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < grid.columns(); columnIndex++) {
                Position position = new Position(rowIndex, columnIndex);
                int index = grid.indexOf(position);
                assertEquals(rowIndex * 5 + columnIndex, index, "Row-major index");
                assertEquals(position, grid.positionAt(index), "Coordinate round trip");
            }
        }
        assertTrue(grid.contains(2, 4), "Last cell is in bounds");
        assertFalse(grid.contains(-1, 0));
        assertFalse(grid.contains(0, -1));
        assertFalse(grid.contains(3, 0));
        assertFalse(grid.contains(0, 5));
    }

    @Test
    void openGridDefaults() {
        Grid grid = new Grid(2, 3);
        assertEquals(100, grid.maxTerrainCost());
        for (int index = 0; index < grid.size(); index++) {
            assertFalse(grid.isBlocked(index), "New cells are traversable");
            assertEquals(1, grid.terrainCost(index), "Default terrain cost");
        }
    }

    @Test
    void neighborsAtEveryBoundary() {
        Grid grid = new Grid(3, 4);
        int[][] expected = {
            {1, 4}, {2, 5, 0}, {3, 6, 1}, {7, 2},
            {0, 5, 8}, {1, 6, 9, 4}, {2, 7, 10, 5}, {3, 11, 6},
            {4, 9}, {5, 10, 8}, {6, 11, 9}, {7, 10}
        };
        int[] buffer = new int[Grid.MAX_NEIGHBORS];
        for (int index = 0; index < grid.size(); index++) {
            Arrays.fill(buffer, -1);
            int count = grid.neighbors(index, buffer);
            assertArrayEquals(expected[index], Arrays.copyOf(buffer, count),
                    "Neighbor order and boundaries at " + index);
            for (int remaining = count; remaining < buffer.length; remaining++) {
                assertEquals(-1, buffer[remaining], "Unused buffer entries remain unchanged");
            }
        }
    }

    @Test
    void narrowGrids() {
        assertNeighbors(new Grid(1, 1), 0);
        assertNeighbors(new Grid(1, 3), 0, 1);
        assertNeighbors(new Grid(1, 3), 1, 2, 0);
        assertNeighbors(new Grid(1, 3), 2, 1);
        assertNeighbors(new Grid(3, 1), 0, 1);
        assertNeighbors(new Grid(3, 1), 1, 0, 2);
        assertNeighbors(new Grid(3, 1), 2, 1);
    }

    @Test
    void wallsAndTerrain() {
        boolean[] walls = {false, true, false, false, false, false};
        int[] costs = {1, 7, 1, 1, 9, 100};
        Grid grid = new Grid(2, 3, walls, costs);
        assertTrue(grid.isBlocked(1), "Wall is impassable");
        assertEquals(7, grid.terrainCost(1), "Walls retain underlying terrain");
        assertFalse(grid.isBlocked(4), "Difficult terrain is traversable");
        assertEquals(9, grid.terrainCost(4));
        assertEquals(100, grid.terrainCost(5), "The hard cap is inclusive");
        assertNeighbors(grid, 0, 3);
        assertNeighbors(grid, 1);
        assertNeighbors(grid, 4, 5, 3);

        Grid enclosed = new Grid(1, 3, new boolean[] {true, false, true}, new int[] {1, 1, 1});
        assertNeighbors(enclosed, 1);

        Grid verticalBarrier = new Grid(3, 1, new boolean[] {false, true, false}, new int[] {1, 1, 1});
        assertNeighbors(verticalBarrier, 0);
        assertNeighbors(verticalBarrier, 2);
    }

    @Test
    void defensiveCopies() {
        boolean[] walls = {false, true};
        int[] costs = {2, 3};
        Grid grid = new Grid(1, 2, walls, costs);
        walls[0] = true;
        walls[1] = false;
        costs[0] = 0;
        assertFalse(grid.isBlocked(0), "Wall input is copied");
        assertTrue(grid.isBlocked(1), "Wall input is copied");
        assertEquals(2, grid.terrainCost(0), "Cost input is copied");
        int[] buffer = new int[Grid.MAX_NEIGHBORS];
        Grid open = new Grid(1, 2);
        open.neighbors(0, buffer);
        buffer[0] = 99;
        assertNeighbors(open, 0, 1);
    }

    @Test
    void configurableTerrainLimit() {
        Grid open = new Grid(2, 3, 20);
        assertEquals(20, open.maxTerrainCost());
        assertEquals(1, open.terrainCost(0));

        Grid weighted = new Grid(1, 2, new boolean[2], new int[] {1, 20}, 20);
        assertEquals(20, weighted.maxTerrainCost());
        assertEquals(20, weighted.terrainCost(1));
        assertEquals(1, new Grid(1, 1, 1).maxTerrainCost());
    }

    @Test
    void rejectsInvalidTerrainLimits() {
        for (int limit : new int[] {-1, 0, 101, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> new Grid(1, 1, limit));
            assertThrows(IllegalArgumentException.class,
                    () -> new Grid(1, 1, new boolean[1], new int[] {1}, limit));
        }
    }

    @Test
    void rejectsCostsAboveTerrainLimit() {
        assertThrows(IllegalArgumentException.class,
                () -> new Grid(1, 1, new boolean[1], new int[] {101}));
        assertThrows(IllegalArgumentException.class,
                () -> new Grid(1, 1, new boolean[1], new int[] {21}, 20));
        assertThrows(IllegalArgumentException.class,
                () -> new Grid(1, 1, new boolean[] {true}, new int[] {21}, 20));
    }

    @Test
    void equalGridsHaveEqualHashes() {
        boolean[] walls = {false, true};
        int[] costs = {1, 5};
        Grid first = new Grid(1, 2, walls, costs, 10);
        Grid second = new Grid(1, 2, new boolean[] {false, true}, new int[] {1, 5}, 10);
        Grid third = new Grid(1, 2, new boolean[] {false, true}, new int[] {1, 5}, 10);

        assertTrue(first.equals(first));
        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(second, third);
        assertEquals(first, third);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(new Grid(1, 2), new Grid(1, 2, new boolean[2], new int[] {1, 1}, 100));

        var grids = new HashSet<Grid>();
        grids.add(first);
        assertTrue(grids.contains(second), "Equal grids can be looked up by value");
        assertFalse(grids.add(second), "Equal grids are deduplicated");

        walls[0] = true;
        costs[0] = 9;
        assertEquals(second, first, "Changing constructor inputs preserves grid equality");
        assertTrue(grids.contains(first), "Input changes preserve hash-based lookup");
    }

    @Test
    void unequalGridsDifferByTheirValues() {
        Grid grid = new Grid(1, 2, new boolean[2], new int[] {1, 1}, 10);
        assertNotEquals(grid, new Grid(2, 1, new boolean[2], new int[] {1, 1}, 10));
        assertNotEquals(grid, new Grid(1, 3, new boolean[3], new int[] {1, 1, 1}, 10));
        assertNotEquals(grid, new Grid(1, 2, new boolean[] {true, false}, new int[] {1, 1}, 10));
        assertNotEquals(grid, new Grid(1, 2, new boolean[2], new int[] {1, 2}, 10));
        assertNotEquals(grid, new Grid(1, 2, new boolean[2], new int[] {1, 1}, 20));
        assertNotEquals(
                new Grid(1, 1, new boolean[] {true}, new int[] {1}, 10),
                new Grid(1, 1, new boolean[] {true}, new int[] {2}, 10),
                "Terrain beneath walls is part of the grid value");
        assertFalse(grid.equals(null));
        assertFalse(grid.equals(new Object()));
    }

    @Test
    void invalidInput() {
        assertThrows(IllegalArgumentException.class, () -> new Grid(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Grid(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Grid(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Grid(1, -1));
        assertThrows(IllegalArgumentException.class, () -> new Grid(50_000, 50_000));
        assertThrows(NullPointerException.class, () -> new Grid(1, 1, null, new int[] {1}));
        assertThrows(NullPointerException.class, () -> new Grid(1, 1, new boolean[1], null));
        assertThrows(IllegalArgumentException.class, () -> new Grid(1, 2, new boolean[1], new int[] {1, 1}));
        assertThrows(IllegalArgumentException.class, () -> new Grid(1, 2, new boolean[2], new int[] {1}));
        assertThrows(IllegalArgumentException.class, () -> new Grid(1, 1, new boolean[1], new int[] {0}));
        assertThrows(IllegalArgumentException.class, () -> new Grid(1, 1, new boolean[] {true}, new int[] {-1}));

        Grid grid = new Grid(2, 3);
        assertThrows(NullPointerException.class, () -> grid.indexOf(null));
        assertThrows(IndexOutOfBoundsException.class, () -> grid.indexOf(new Position(2, 0)));
        assertThrows(IndexOutOfBoundsException.class, () -> grid.indexOf(new Position(0, 3)));
        for (int index : new int[] {-1, grid.size()}) {
            assertThrows(IndexOutOfBoundsException.class, () -> grid.positionAt(index));
            assertThrows(IndexOutOfBoundsException.class, () -> grid.isBlocked(index));
            assertThrows(IndexOutOfBoundsException.class, () -> grid.terrainCost(index));
            assertThrows(IndexOutOfBoundsException.class, () -> grid.neighbors(index, new int[4]));
        }
        assertThrows(NullPointerException.class, () -> grid.neighbors(0, null));
        assertThrows(IllegalArgumentException.class, () -> grid.neighbors(0, new int[3]));
    }

    private static void assertNeighbors(Grid grid, int index, int... expected) {
        int[] buffer = new int[Grid.MAX_NEIGHBORS];
        int count = grid.neighbors(index, buffer);
        assertArrayEquals(expected, Arrays.copyOf(buffer, count), "Neighbors at " + index);
    }
}
