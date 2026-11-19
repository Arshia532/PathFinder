package pathfinder.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathFinderServerTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String MAZE_REQUEST =
            "{\"width\":30,\"height\":21,\"seed\":\"485161065432132\",\"mode\":\"maze\"}";

    @Test
    void generatesASolvableMazeAndReturnsSearchPlayback() throws Exception {
        HttpServer server = PathFinderServer.start(0);
        try (HttpClient client = HttpClient.newHttpClient()) {
            int port = server.getAddress().getPort();
            JsonNode maze = post(client, port, "/api/maps/generate", MAZE_REQUEST, 200);
            assertEquals(maze, post(client, port, "/api/maps/generate", MAZE_REQUEST, 200));
            assertNotEquals(maze, post(client, port, "/api/maps/generate",
                    MAZE_REQUEST.replace("485161065432132", "42"), 200));
            assertEquals(30, maze.path("width").intValue());
            assertEquals(21, maze.path("height").intValue());
            JsonNode bfs = solve(client, port, maze, "bfs");
            JsonNode dfs = solve(client, port, maze, "dfs");
            assertEquals(120, bfs.path("stats").path("pathLength").intValue());
            assertEquals(bfs.path("path"), dfs.path("path"));
            for (JsonNode result : new JsonNode[] {bfs, dfs}) {
                assertTrue(result.path("found").booleanValue());
                assertEquals(result.path("stats").path("visited").intValue(), result.path("steps").size());
                assertEquals(maze.path("start"), result.path("path").get(0));
                assertEquals(maze.path("end"), result.path("steps").get(result.path("steps").size() - 1).path("expanded"));
            }
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rejectsAnOutOfRangeSeedWithoutRoundingIt() throws Exception {
        HttpServer server = PathFinderServer.start(0);
        try (HttpClient client = HttpClient.newHttpClient()) {
            int port = server.getAddress().getPort();
            post(client, port, "/api/maps/generate", MAZE_REQUEST.replace("485161065432132", "9223372036854775807"), 200);
            JsonNode error = post(client, port, "/api/maps/generate",
                    MAZE_REQUEST.replace("485161065432132", "9223372036854775808"), 400);
            assertTrue(error.path("message").asText().contains("whole number"));
        } finally {
            server.stop(0);
        }
    }

    private static JsonNode solve(HttpClient client, int port, JsonNode maze, String algorithm)
            throws IOException, InterruptedException {
        String payload = maze.toString().replaceFirst("}$", ",\"algorithm\":\"" + algorithm + "\",\"bidirectional\":false}");
        return post(client, port, "/api/search", payload, 200);
    }

    private static JsonNode post(HttpClient client, int port, String path, String body, int status)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(status, response.statusCode(), response.body());
        return JSON.readTree(response.body());
    }
}
