package pathfinder.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import pathfinder.generation.RandomGridGenerator;
import pathfinder.grid.Grid;
import pathfinder.grid.Position;

/** Creates maps for the map endpoints. */
@Service
final class MapService {
    static final double WALL_PROBABILITY = 0.30;

    private final RandomGridGenerator generator = new RandomGridGenerator();

    /**
     * Generates a random map with open start and end cells.
     *
     * @param request the size and seed
     * @return the generated map
     * @throws InvalidRequestException if the size is out of range
     */
    GridMap random(RandomMapRequest request) {
        int width = request.width();
        int height = request.height();
        if (!inRange(width) || !inRange(height)) {
            throw new InvalidRequestException("Width and height must be from "
                    + SearchService.MIN_SIZE + " to " + SearchService.MAX_SIZE + ".");
        }

        Grid grid = generator.generate(height, width, request.seed(), WALL_PROBABILITY);
        // Same default spots as createMap in frontend/src/state/appState.ts
        Position start = new Position(height - 6, 3);
        Position end = new Position(5, width - 4);

        List<Position> walls = new ArrayList<>();
        for (int index = 0; index < grid.size(); index++) {
            Position cell = grid.positionAt(index);
            if (grid.isBlocked(index) && !cell.equals(start) && !cell.equals(end)) {
                walls.add(cell);
            }
        }
        return new GridMap(width, height, start, end, walls);
    }

    private static boolean inRange(int size) {
        return size >= SearchService.MIN_SIZE && size <= SearchService.MAX_SIZE;
    }
}