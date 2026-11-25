package pathfinder.search;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import pathfinder.generation.RandomGridGenerator;
import pathfinder.grid.Grid;
import pathfinder.grid.Position;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Shared grid builders and assertions for search tests. */
final class SearchFixtures {
    private SearchFixtures() {
    }

    /**
     * Builds a grid from text rows: {@code #} is a wall, {@code .} costs 1, and
     * a digit from 1 to 9 is that terrain cost.
     */
    static Grid parse(String... rows) {
        int columns = rows[0].length();
        boolean[] walls = new boolean[rows.length * columns];
        int[] costs = new int[walls.length];
        for (int row = 0; row < rows.length; row++) {
            for (int column = 0; column < columns; column++) {
                char symbol = rows[row].charAt(column);
                int index = row * columns + column;
                walls[index] = symbol == '#';
                costs[index] = Character.isDigit(symbol) ? symbol - '0' : 1;
            }
        }
        return new Grid(rows.length, columns, walls, costs);
    }

    /**
     * Builds a repeatable random map with walls from {@link RandomGridGenerator},
     * terrain costs from 1 to 9, and open top-left and bottom-right corners.
     */
    static Grid randomWeighted(long seed, int rows, int columns, double wallProbability) {
        return randomWeighted(seed, rows, columns, wallProbability, 9);
    }

    /**
     * Builds a repeatable random map with walls from {@link RandomGridGenerator},
     * terrain costs from 1 to {@code maxCost}, and open top-left and bottom-right
     * corners.
     */
    static Grid randomWeighted(long seed, int rows, int columns,
                               double wallProbability, int maxCost) {
        Grid generated = new RandomGridGenerator().generate(rows, columns, seed, wallProbability);
        boolean[] walls = new boolean[generated.size()];
        int[] costs = new int[generated.size()];
        Random random = new Random(seed);
        for (int index = 0; index < walls.length; index++) {
            walls[index] = generated.isBlocked(index);
            costs[index] = 1 + random.nextInt(maxCost);
        }
        walls[0] = false;
        walls[walls.length - 1] = false;
        return new Grid(rows, columns, walls, costs, maxCost);
    }

    /** Picks a random traversable cell; the grid must have at least one. */
    static Position randomOpenCell(Grid grid, Random random) {
        while (true) {
            int index = random.nextInt(grid.size());
            if (!grid.isBlocked(index)) {
                return grid.positionAt(index);
            }
        }
    }

    /**
     * Computes the fewest moves from {@code start} to every cell with a plain
     * breadth-first flood fill that shares no code with the search package.
     * Unreachable cells and walls get {@code -1}.
     */
    static int[] referenceDistances(Grid grid, Position start) {
        int[] distance = new int[grid.size()];
        Arrays.fill(distance, -1);
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        int[] neighbors = new int[Grid.MAX_NEIGHBORS];
        distance[grid.indexOf(start)] = 0;
        queue.add(grid.indexOf(start));
        while (!queue.isEmpty()) {
            int cell = queue.remove();
            int count = grid.neighbors(cell, neighbors);
            for (int i = 0; i < count; i++) {
                if (distance[neighbors[i]] < 0) {
                    distance[neighbors[i]] = distance[cell] + 1;
                    queue.add(neighbors[i]);
                }
            }
        }
        return distance;
    }

    /**
     * Computes the lowest path cost from {@code start} to every cell by
     * repeated Bellman-Ford edge relaxation, sharing no code or priority-queue
     * logic with the search package. Unreachable cells and walls get {@code -1}.
     */
    static long[] referenceCosts(Grid grid, Position start) {
        long[] cost = new long[grid.size()];
        Arrays.fill(cost, -1L);
        cost[grid.indexOf(start)] = 0L;
        int[] neighbors = new int[Grid.MAX_NEIGHBORS];
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int cell = 0; cell < grid.size(); cell++) {
                if (cost[cell] < 0) {
                    continue;
                }
                int count = grid.neighbors(cell, neighbors);
                for (int i = 0; i < count; i++) {
                    int next = neighbors[i];
                    long candidate = cost[cell] + grid.terrainCost(next);
                    if (cost[next] < 0 || candidate < cost[next]) {
                        cost[next] = candidate;
                        changed = true;
                    }
                }
            }
        }
        return cost;
    }

    /** Returns the bottom-right cell, used as the goal on random maps. */
    static Position lastCell(Grid grid) {
        return new Position(grid.rows() - 1, grid.columns() - 1);
    }

    /**
     * Checks that a found path joins the endpoints through adjacent open cells
     * and that its reported cost is the sum of entry costs after the start.
     */
    static void assertValidPath(Grid grid, Position start, Position goal, SearchResult result) {
        List<Position> path = result.path();
        assertEquals(start, path.getFirst(), "Path starts at the start");
        assertEquals(goal, path.getLast(), "Path ends at the goal");
        long cost = 0;
        for (int step = 1; step < path.size(); step++) {
            Position from = path.get(step - 1);
            Position to = path.get(step);
            int distance = Math.abs(from.row() - to.row()) + Math.abs(from.column() - to.column());
            assertEquals(1, distance, "Each move is to an orthogonal neighbor");
            assertFalse(grid.isBlocked(grid.indexOf(to)), "Path never enters a wall");
            cost += grid.terrainCost(grid.indexOf(to));
        }
        assertEquals(cost, result.cost(), "Cost sums entry costs");
        assertEquals(path.size() - 1, result.moves());
    }
}
