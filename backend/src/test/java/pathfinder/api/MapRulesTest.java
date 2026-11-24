package pathfinder.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import pathfinder.grid.Position;
import pathfinder.search.SearchAlgorithm;

class MapRulesTest {
    private static final Position START = new Position(0, 0);
    private static final Position END = new Position(9, 9);

    @Test
    void removesDuplicateWallsAndKeepsTheirOrder() {
        Position a = new Position(1, 1);
        Position b = new Position(2, 2);

        GridMap checked = MapRules.check(new GridMap(10, 10, START, END, List.of(a, b, a)));

        assertEquals(List.of(a, b), checked.walls());
    }

    @Test
    void rejectsAWallOnTheEnd() {
        InvalidRequestException error = assertThrows(InvalidRequestException.class,
                () -> MapRules.check(new GridMap(10, 10, START, END, List.of(END))));

        assertEquals(SearchAlgorithm.DESTINATION_BLOCKED_MESSAGE, error.getMessage());
    }

    @Test
    void rejectsAMissingEnd() {
        InvalidRequestException error = assertThrows(InvalidRequestException.class,
                () -> MapRules.check(new GridMap(10, 10, START, null, List.of())));

        assertEquals("Start and end are required.", error.getMessage());
    }
}