package pathfinder.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import pathfinder.search.SearchAlgorithm;

@WebMvcTest(SearchController.class)
@Import(SearchService.class)
class SearchControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void bfsFindsTheShortestPathAroundAWall() {
        String walls = cell(3, 5) + "," + cell(4, 5) + "," + cell(5, 5);
        MvcTestResult result = search(body(10, cell(4, 1), cell(4, 8), walls, "bfs"));

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.found").isEqualTo(true);
        assertThat(result).bodyJson().extractingPath("$.stats.pathLength").isEqualTo(11);
        assertThat(result).bodyJson().extractingPath("$.path").asArray().hasSize(12);
        assertThat(result).bodyJson().extractingPath("$.steps[0].side").isEqualTo("forward");
    }

    @Test
    void unreachableEndIsANormalResult() {
        String walls = cell(0, 1) + "," + cell(1, 0);
        MvcTestResult result = search(body(10, cell(0, 0), cell(9, 9), walls, "dfs"));

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.found").isEqualTo(false);
        assertThat(result).bodyJson().extractingPath("$.path").asArray().isEmpty();
        assertThat(result).bodyJson().extractingPath("$.stats.pathLength").isNull();
    }

    @Test
    void startOnAWallIsRejected() {
        assertBadRequest(body(10, cell(0, 0), cell(9, 9), cell(0, 0), "bfs"),
                SearchAlgorithm.START_BLOCKED_MESSAGE);
    }

    @Test
    void wallOutsideTheGridIsRejected() {
        MvcTestResult result = search(body(10, cell(0, 0), cell(9, 9), cell(0, 12), "bfs"));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.message").asString()
                .startsWith("Wall (12, 0) is outside the 10");
    }

    @Test
    void sizeBelowTheMinimumIsRejected() {
        assertBadRequest(body(5, cell(0, 0), cell(4, 4), "", "bfs"),
                "Width and height must be from 10 to 50.");
    }

    @Test
    void dijkstraAndAStarFindShortestPathsAndAStarVisitsFewerCells() {
        String walls = cell(3, 5) + "," + cell(4, 5) + "," + cell(5, 5);
        MvcTestResult dijkstra = search(body(10, cell(4, 1), cell(4, 8), walls, "dijkstra"));
        MvcTestResult astar = search(body(10, cell(4, 1), cell(4, 8), walls, "astar"));

        assertThat(dijkstra).hasStatusOk();
        assertThat(dijkstra).bodyJson().extractingPath("$.found").isEqualTo(true);
        assertThat(dijkstra).bodyJson().extractingPath("$.stats.pathLength").isEqualTo(11);
        assertThat(dijkstra).bodyJson().extractingPath("$.stats.pathCost").isEqualTo(11);

        assertThat(astar).hasStatusOk();
        assertThat(astar).bodyJson().extractingPath("$.found").isEqualTo(true);
        assertThat(astar).bodyJson().extractingPath("$.stats.pathLength").isEqualTo(11);
        assertThat(astar).bodyJson().extractingPath("$.stats.pathCost").isEqualTo(11);

        int dijkstraVisited = (Integer) assertThat(dijkstra).bodyJson()
                .extractingPath("$.stats.visited").actual();
        int astarVisited = (Integer) assertThat(astar).bodyJson()
                .extractingPath("$.stats.visited").actual();
        assertThat(astarVisited).isLessThan(dijkstraVisited);
    }

    @Test
    void bidirectionalSearchIsRejectedUntilImplemented() {
        String json = """
                {"width": 10, "height": 10, "start": %s, "end": %s,
                    "walls": [], "algorithm": "astar", "bidirectional": true}
                """.formatted(cell(0, 0), cell(9, 9));
        assertBadRequest(json, "Bi-directional search is not available yet.");
    }

    @Test
    void malformedJsonIsRejected() {
        assertBadRequest("{\"width\": 10,", "The request is not valid JSON or has the wrong shape.");
    }

    @Test
    void unknownAlgorithmIsRejected() {
        assertBadRequest(body(10, cell(0, 0), cell(9, 9), "", "teleport"),
                "The request is not valid JSON or has the wrong shape.");
    }

    @Test
    void negativeCoordinateIsRejected() {
        assertBadRequest(body(10, cell(-1, 0), cell(9, 9), "", "bfs"),
                "The request is not valid JSON or has the wrong shape.");
    }

    private MvcTestResult search(String json) {
        return mvc.post().uri("/api/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private void assertBadRequest(String json, String message) {
        MvcTestResult result = search(json);
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.message").isEqualTo(message);
    }

    private static String body(int size, String start, String end, String walls, String algorithm) {
        return """
                {"width": %d, "height": %d, "start": %s, "end": %s,
                    "walls": [%s], "algorithm": "%s", "bidirectional": false}
                """.formatted(size, size, start, end, walls, algorithm);
    }

    private static String cell(int row, int column) {
        return "{\"row\": %d, \"column\": %d}".formatted(row, column);
    }
}