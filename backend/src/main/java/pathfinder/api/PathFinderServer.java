package pathfinder.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/** Serves generated maps and Java search results to the frontend. */
public final class PathFinderServer {
    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_REQUEST_BYTES = 128 * 1024;
    private static final ObjectMapper JSON = new ObjectMapper()
            .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    private PathFinderServer() {
    }

    /**
     * Starts the local API, which listens only on the loopback interface.
     * @param args the optional port number
     * @throws IOException if the server cannot bind
     */
    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? DEFAULT_PORT : Integer.parseInt(args[0]);
        HttpServer server = start(port);
        System.out.println("PathFinder API listening at http://127.0.0.1:" + server.getAddress().getPort());
    }

    static HttpServer start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        register(server, "/api/maps/generate", PathFinderApi.GenerateRequest.class, PathFinderApi::generate);
        register(server, "/api/search", PathFinderApi.SearchRequest.class, PathFinderApi::search);
        server.start();
        return server;
    }

    private static <T> void register(HttpServer server, String path, Class<T> requestType,
                                     Function<T, ?> handler) {
        server.createContext(path, exchange -> {
            try {
                if (!path.equals(exchange.getRequestURI().getPath())) {
                    respond(exchange, 404, Map.of("message", "Endpoint not found."));
                    return;
                }
                if (!"POST".equals(exchange.getRequestMethod())) {
                    exchange.getResponseHeaders().set("Allow", "POST");
                    respond(exchange, 405, Map.of("message", "Use POST for this endpoint."));
                    return;
                }

                byte[] body = exchange.getRequestBody().readNBytes(MAX_REQUEST_BYTES + 1);
                if (body.length > MAX_REQUEST_BYTES) {
                    respond(exchange, 413, Map.of("message", "The request is too large."));
                    return;
                }

                Object result;
                try {
                    T request = JSON.readValue(body, requestType);
                    if (request == null) {
                        throw new IllegalArgumentException("Provide a JSON request.");
                    }
                    result = handler.apply(request);
                } catch (JsonProcessingException exception) {
                    respond(exchange, 400, Map.of("message", "Invalid request JSON."));
                    return;
                } catch (IllegalArgumentException exception) {
                    respond(exchange, 400, Map.of("message", exception.getMessage()));
                    return;
                }
                respond(exchange, 200, result);
            } finally {
                exchange.close();
            }
        });
    }

    private static void respond(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] json = JSON.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, json.length);
        exchange.getResponseBody().write(json);
    }
}
