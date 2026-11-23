package pathfinder.api;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import pathfinder.grid.Position;
import pathfinder.search.SearchAlgorithm;

/** The rules every map must follow; shared by the search and map endpoints. */
final class MapRules {
    static final int MIN_SIZE = 10;
    static final int MAX_SIZE = 50;

    private MapRules() {
    }

    /**
     * Checks a map and returns a copy without duplicate walls.
     *
     * @param map the map to check
     * @return the checked map
     * @throws InvalidRequestException if the map breaks a rule
     */
    static GridMap check(GridMap map) {
        checkSize(map.width(), map.height());
        require(map.start() != null && map.end() != null, "Start and end are required.");
        requireInside(map.start(), "Start", map);
        requireInside(map.end(), "End", map);

        Set<Position> walls = new LinkedHashSet<>();
        for (Position wall : map.walls()) {
            requireInside(wall, "Wall", map);
            walls.add(wall);
        }
        require(!walls.contains(map.start()), SearchAlgorithm.START_BLOCKED_MESSAGE);
        require(!walls.contains(map.end()), SearchAlgorithm.DESTINATION_BLOCKED_MESSAGE);
        return new GridMap(map.width(), map.height(), map.start(), map.end(), List.copyOf(walls));
    }

    /**
     * Checks that a size is within the supported limits.
     *
     * @param width  the number of columns
     * @param height the number of rows
     * @throws InvalidRequestException if either is out of range
     */
    static void checkSize(int width, int height) {
        require(inRange(width) && inRange(height),
                "Width and height must be from " + MIN_SIZE + " to " + MAX_SIZE + ".");
    }

    private static void requireInside(Position cell, String name, GridMap map) {
        require(cell.row() < map.height() && cell.column() < map.width(),
                name + " (" + cell.column() + ", " + cell.row() + ") is outside the "
                        + map.width() + " × " + map.height() + " grid.");
    }

    private static boolean inRange(int size) {
        return size >= MIN_SIZE && size <= MAX_SIZE;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new InvalidRequestException(message);
        }
    }
}