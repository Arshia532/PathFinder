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

    private MvcTestResult random(String json) {
        return mvc.post().uri("/api/maps/random")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }
}