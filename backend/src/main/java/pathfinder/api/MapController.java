package pathfinder.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

/** HTTP entry point for map endpoints under {@code /api/maps}. */
@RestController
@RequestMapping("/api/maps")
final class MapController {
    private final MapService service;

    MapController(MapService service) {
        this.service = service;
    }

    /**
     * Generates a random map.
     *
     * @param request the size and seed
     * @return the map as JSON
     */
    @PostMapping("/random")
    GridMap random(@RequestBody RandomMapRequest request) {
        return service.random(request);
    }

    /**
     * Generates a maze.
     *
     * @param request the size and seed
     * @return the maze as JSON
     */
    @PostMapping("/maze")
    GridMap maze(@RequestBody RandomMapRequest request) {
        return service.maze(request);
    }

    /**
     * Returns a map as the contents of a map file.
     *
     * @param map the map to save
     * @return pretty-printed JSON
     */
    @PostMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    String export(@RequestBody GridMap map) {
        return service.export(map);
    }

    /**
     * Reads a map file and returns the checked map.
     *
     * @param text the file's contents, unparsed
     * @return the map as JSON
     */
    @PostMapping("/import")
    GridMap importMap(@RequestBody String text) {
        return service.importMap(text);
    }
}