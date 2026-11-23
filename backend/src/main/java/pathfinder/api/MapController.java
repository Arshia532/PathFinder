package pathfinder.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}