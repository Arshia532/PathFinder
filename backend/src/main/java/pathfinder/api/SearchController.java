package pathfinder.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry point for searches: {@code POST /api/search}. */
@RestController
@RequestMapping("/api")
final class SearchController {
    private final SearchService service;

    SearchController(SearchService service) {
        this.service = service;
    }

    /**
     * Runs a search on the map in the request body.
     *
     * @param request the map and search settings
     * @return the search result as JSON
     */
    @PostMapping("/search")
    SearchResponse search(@RequestBody SearchRequest request) {
        return service.search(request);
    }
}