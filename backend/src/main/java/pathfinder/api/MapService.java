package pathfinder.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import pathfinder.generation.RandomGridGenerator;
import pathfinder.grid.Grid;
import pathfinder.grid.Position;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/** Creates, saves, and loads maps for the map endpoints. */
@Service
final class MapService {
    static final double WALL_PROBABILITY = 0.30;
    static final String NOT_A_MAP = "This file isn't a valid PathFinder map.";

    private final RandomGridGenerator generator = new RandomGridGenerator();
    private final JsonMapper json;

    MapService(JsonMapper json) {
        this.json = json;
    }

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
        MapRules.checkSize(width, height);

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

    /**
     * Turns a map into the contents of a map file.
     *
     * @param map the map to save
     * @return the map as pretty-printed JSON
     * @throws InvalidRequestException if the map breaks a rule
     */
    String export(GridMap map) {
        return json.writerWithDefaultPrettyPrinter().writeValueAsString(MapRules.check(map));
    }

    /**
     * Reads a map file. Unknown fields are ignored and duplicate walls removed.
     *
     * @param text the file's contents
     * @return the checked map
     * @throws InvalidRequestException if the text is not a map or the map breaks a
     *                                 rule
     */
    GridMap importMap(String text) {
        GridMap map;
        try {
            map = json.readerFor(GridMap.class)
                    .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(text);
        } catch (JacksonException exception) {
            throw new InvalidRequestException(NOT_A_MAP);
        }
        if (map == null) {
            throw new InvalidRequestException(NOT_A_MAP);
        }
        return MapRules.check(map);
    }
}