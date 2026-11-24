package pathfinder.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import pathfinder.grid.Grid;
import pathfinder.grid.Position;
import pathfinder.search.BreadthFirstSearch;

class MapServiceTest {
    private final MapService service = new MapService(JsonMapper.builder().build());

    @Test
    void sameSeedAndSizeGiveTheSameMap() {
        RandomMapRequest request = new RandomMapRequest(30, 21, 48213L);

        assertEquals(service.random(request), service.random(request));
    }

    @Test
    void startAndEndAreNeverWallsAndWallsStayInside() {
        for (long seed = 0; seed < 50; seed++) {
            GridMap map = service.random(new RandomMapRequest(30, 21, seed));

            assertFalse(map.walls().contains(map.start()), "start is a wall, seed " + seed);
            assertFalse(map.walls().contains(map.end()), "end is a wall, seed " + seed);
            for (Position wall : map.walls()) {
                assertTrue(wall.row() < 21 && wall.column() < 30, "outside: " + wall);
            }
        }
    }

    @Test
    void sameSeedAndSizeGiveTheSameMaze() {
        RandomMapRequest request = new RandomMapRequest(30, 21, 48213L);

        assertEquals(service.maze(request), service.maze(request));
    }

    @Test
    void mazeEndpointsAreOpenCornersJoinedByARoute() {
        for (long seed = 0; seed < 20; seed++) {
            GridMap map = service.maze(new RandomMapRequest(30, 21, seed));

            assertEquals(new Position(1, 1), map.start());
            assertEquals(new Position(19, 29), map.end());
            assertFalse(map.walls().contains(map.start()), "start is a wall, seed " + seed);
            assertFalse(map.walls().contains(map.end()), "end is a wall, seed " + seed);
            assertTrue(new BreadthFirstSearch().search(toGrid(map), map.start(), map.end()).found(),
                    "no route, seed " + seed);
        }
    }

    @Test
    void exportThenImportGivesTheSameMap() {
        GridMap map = service.random(new RandomMapRequest(30, 21, 48213L));

        assertEquals(map, service.importMap(service.export(map)));
    }

    private static Grid toGrid(GridMap map) {
        boolean[] walls = new boolean[map.width() * map.height()];
        for (Position wall : map.walls()) {
            walls[wall.row() * map.width() + wall.column()] = true;
        }
        int[] costs = new int[walls.length];
        Arrays.fill(costs, 1);
        return new Grid(map.height(), map.width(), walls, costs);
    }
}
