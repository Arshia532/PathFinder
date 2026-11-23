package pathfinder.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import pathfinder.grid.Position;

class MapServiceTest {
    private final MapService service = new MapService();

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
}