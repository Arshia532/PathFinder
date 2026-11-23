package pathfinder.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import pathfinder.grid.Position;
import tools.jackson.databind.json.JsonMapper;

/**
 * Checks that the records match the JSON shapes in frontend/src/api/types.ts.
 */
class ApiJsonTest {
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void readsTheFrontendSearchRequest() {
        String body = """
                {"width": 10, "height": 10,
                    "start": {"row": 4, "column": 1}, "end": {"row": 4, "column": 8},
                    "walls": [{"row": 3, "column": 5}],
                    "algorithm": "astar", "bidirectional": false}
                """;

        SearchRequest request = json.readValue(body, SearchRequest.class);

        assertEquals(10, request.width());
        assertEquals(new Position(4, 1), request.start());
        assertEquals(List.of(new Position(3, 5)), request.walls());
        assertEquals(AlgorithmId.ASTAR, request.algorithm());
    }

    @Test
    void missingWallsBecomeAnEmptyList() {
        String body = """
                {"width": 10, "height": 10,
                    "start": {"row": 0, "column": 0}, "end": {"row": 9, "column": 9},
                    "algorithm": "bfs", "bidirectional": false}
                """;

        assertEquals(List.of(), json.readValue(body, SearchRequest.class).walls());
    }

    @Test
    void writesLowercaseSideAndNullStatsWhenNotFound() {
        SearchResponse response = new SearchResponse(
                false,
                List.of(new SearchStep(new Position(0, 0), List.of(), SearchSide.FORWARD)),
                List.of(),
                new SearchStats(1, null, null));

        String text = json.writeValueAsString(response);

        assertTrue(text.contains("\"side\":\"forward\""), text);
        assertTrue(text.contains("\"pathLength\":null"), text);
        assertTrue(text.contains("\"pathCost\":null"), text);
    }
}