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

@WebMvcTest(MapController.class)
@Import(MapService.class)
class MapControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void returnsAMapOfTheRequestedSize() {
        MvcTestResult result = random("{\"width\": 30, \"height\": 21, \"seed\": 48213}");

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.width").isEqualTo(30);
        assertThat(result).bodyJson().extractingPath("$.height").isEqualTo(21);
        assertThat(result).bodyJson().extractingPath("$.start.row").isEqualTo(15);
        assertThat(result).bodyJson().extractingPath("$.end.column").isEqualTo(26);
    }

    @Test
    void sizeOutOfRangeIsRejected() {
        MvcTestResult result = random("{\"width\": 60, \"height\": 21, \"seed\": 1}");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.message")
                .isEqualTo("Width and height must be from 10 to 50.");
    }

    @Test
    void returnsAMazeOfTheRequestedSize() {
        MvcTestResult result = post("/api/maps/maze", "{\"width\": 30, \"height\": 21, \"seed\": 48213}");

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.width").isEqualTo(30);
        assertThat(result).bodyJson().extractingPath("$.height").isEqualTo(21);
        assertThat(result).bodyJson().extractingPath("$.start.row").isEqualTo(1);
        assertThat(result).bodyJson().extractingPath("$.end.column").isEqualTo(29);
    }

    @Test
    void mazeSizeOutOfRangeIsRejected() {
        MvcTestResult result = post("/api/maps/maze", "{\"width\": 30, \"height\": 9, \"seed\": 1}");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.message")
                .isEqualTo("Width and height must be from 10 to 50.");
    }

    private MvcTestResult random(String json) {
        return mvc.post().uri("/api/maps/random")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private static final String MAP_WITH_DUPLICATE_WALL = """
            {"width": 10, "height": 10,
                "start": {"row": 0, "column": 0}, "end": {"row": 9, "column": 9},
                "walls": [{"row": 2, "column": 2}, {"row": 2, "column": 2}]}
            """;

    @Test
    void exportReturnsPrettyJsonWithoutDuplicates() {
        MvcTestResult result = post("/api/maps/export", MAP_WITH_DUPLICATE_WALL);

        assertThat(result).hasStatusOk();
        assertThat(result).bodyText().contains("\n");
        assertThat(result).bodyJson().extractingPath("$.walls").asArray().hasSize(1);
    }

    @Test
    void importReturnsACheckedMap() {
        MvcTestResult result = post("/api/maps/import", MAP_WITH_DUPLICATE_WALL);

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.walls").asArray().hasSize(1);
    }

    @Test
    void importRejectsTextThatIsNotAMap() {
        MvcTestResult result = post("/api/maps/import", "{\"width\": 10,");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.message").isEqualTo(MapService.NOT_A_MAP);
    }

    @Test
    void importRejectsAWallOnTheStart() {
        String text = """
                {"width": 10, "height": 10,
                    "start": {"row": 0, "column": 0}, "end": {"row": 9, "column": 9},
                    "walls": [{"row": 0, "column": 0}]}
                """;

        MvcTestResult result = post("/api/maps/import", text);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.message")
                .isEqualTo(SearchAlgorithm.START_BLOCKED_MESSAGE);
    }

    private MvcTestResult post(String uri, String body) {
        return mvc.post().uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();
    }
}