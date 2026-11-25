package pathfinder.api;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import pathfinder.grid.Grid;
import pathfinder.grid.Position;
import pathfinder.search.AStarSearch;
import pathfinder.search.BreadthFirstSearch;
import pathfinder.search.DepthFirstSearch;
import pathfinder.search.DijkstraSearch;
import pathfinder.search.SearchAlgorithm;
import pathfinder.search.SearchResult;

/**
 * Checks a search request, runs the chosen algorithm, and records every step.
 */
@Service
final class SearchService {

    /**
     * Runs one search.
     *
     * @param request the map and search settings
     * @return the recorded steps, path, and stats
     * @throws InvalidRequestException if the request is invalid or bidirectional
     *                                 search is not available yet
     */
    SearchResponse search(SearchRequest request) {
        if (request.algorithm() == null) {
            throw new InvalidRequestException("Choose an algorithm.");
        }
        GridMap map = MapRules.check(new GridMap(
                request.width(), request.height(), request.start(), request.end(), request.walls()));
        if (request.bidirectional()) {
            throw new InvalidRequestException("Bi-directional search is not available yet.");
        }

        Grid grid = toGrid(map);
        StepRecorder recorder = new StepRecorder(grid, SearchSide.FORWARD);
        SearchResult result = algorithmFor(request.algorithm())
                .search(grid, map.start(), map.end(), recorder);

        boolean found = result.found();
        SearchStats stats = new SearchStats(
                result.expandedCount(),
                found ? result.moves() : null,
                found ? result.cost() : null);
        return new SearchResponse(found, recorder.steps(), result.path(), stats);
    }

    private static SearchAlgorithm algorithmFor(AlgorithmId id) {
        return switch (id) {
            case BFS -> new BreadthFirstSearch();
            case DFS -> new DepthFirstSearch();
            case ASTAR -> new AStarSearch();
            case DIJKSTRA -> new DijkstraSearch();
        };
    }

    private static Grid toGrid(GridMap map) {
        int cellCount = map.width() * map.height();
        boolean[] blocked = new boolean[cellCount];
        for (Position wall : map.walls()) {
            blocked[wall.row() * map.width() + wall.column()] = true;
        }
        int[] costs = new int[cellCount];
        Arrays.fill(costs, 1);
        return new Grid(map.height(), map.width(), blocked, costs);
    }
}