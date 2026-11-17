package pathfinder.search;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import pathfinder.grid.Position;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchResultTest {
    @Test
    void storesAnUnmodifiableCopyOfThePath() {
        List<Position> path = new ArrayList<>(List.of(new Position(0, 0), new Position(0, 1)));
        SearchResult result = new SearchResult(path, 3, 2);
        path.clear();
        assertEquals(2, result.path().size(), "Later changes to the input do not leak in");
        assertEquals(1, result.moves());
        assertThrows(UnsupportedOperationException.class, () -> result.path().clear());
    }

    @Test
    void unreachableHasNoPathMovesOrCost() {
        SearchResult result = SearchResult.unreachable(7);
        assertFalse(result.found());
        assertEquals(0, result.moves());
        assertEquals(0, result.cost());
        assertEquals(7, result.expandedCount());
    }

    @Test
    void rejectsInconsistentValues() {
        List<Position> path = List.of(new Position(0, 0));
        assertThrows(NullPointerException.class, () -> new SearchResult(null, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new SearchResult(path, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new SearchResult(path, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new SearchResult(List.of(), 5, 0));
    }
}
