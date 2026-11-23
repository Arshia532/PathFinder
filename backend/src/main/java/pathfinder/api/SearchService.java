package pathfinder.api;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import pathfinder.grid.Grid;
import pathfinder.grid.Position;
import pathfinder.search.BreadthFirstSearch;
import pathfinder.search.DepthFirstSearch;
import pathfinder.search.SearchAlgorithm;
import pathfinder.search.SearchResult;

/**
 * Checks a search request, runs the chosen algorithm, and records every step.
 */
@Service
final class SearchService {
    static final int MIN_SIZE = 10;
    static final int MAX_SIZE = 50;

    /**
     * Runs one search.
     *
     * @param request the map and search settings
     * @return the recorded steps, path, and stats
     * @throws InvalidRequestException if the request is invalid or the algorithm is
     *                                 not available yet
     */
    SearchResponse search(SearchRequest request) {
        validate(request);
        Grid grid = toGrid(request);
        if (grid.isBlocked(grid.indexOf(request.start()))) {
            throw new InvalidRequestException(SearchAlgorithm.START_BLOCKED_MESSAGE);
        }
        if (grid.isBlocked(grid.indexOf(request.end()))) {
            throw new InvalidRequestException(SearchAlgorithm.DESTINATION_BLOCKED_MESSAGE);
        }
        if (request.bidirectional()) {
            throw new InvalidRequestException("Bi-directional search is not available yet.");
        }

        StepRecorder recorder = new StepRecorder(grid, SearchSide.FORWARD);
        SearchResult result = algorithmFor(request.algorithm())
                .search(grid, request.start(), request.end(), recorder);

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
            case ASTAR -> throw new InvalidRequestException("A* is not available yet.");
            case DIJKSTRA -> throw new InvalidRequestException("Dijkstra is not available yet.");
        };
    }

    private static void validate(SearchRequest request) {
        check(request.algorithm() != null, "Choose an algorithm.");
        check(inRange(request.width()) && inRange(request.height()),
                "Width and height must be from " + MIN_SIZE + " to " + MAX_SIZE + ".");
        check(request.start() != null && request.end() != null, "Start and end are required.");
        checkInside(request.start(), "Start", request);
        checkInside(request.end(), "End", request);
        for (Position wall : request.walls()) {
            checkInside(wall, "Wall", request);
        }
    }

    private static Grid toGrid(SearchRequest request) {
        int cellCount = request.width() * request.height();
        boolean[] blocked = new boolean[cellCount];
        for (Position wall : request.walls()) {
            blocked[wall.row() * request.width() + wall.column()] = true;
        }
        int[] costs = new int[cellCount];
        Arrays.fill(costs, 1);
        return new Grid(request.height(), request.width(), blocked, costs);
    }

    private static void checkInside(Position cell, String name, SearchRequest request) {
        check(cell.row() < request.height() && cell.column() < request.width(),
                name + " (" + cell.column() + ", " + cell.row() + ") is outside the "
                        + request.width() + " × " + request.height() + " grid.");
    }

    private static boolean inRange(int size) {
        return size >= MIN_SIZE && size <= MAX_SIZE;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new InvalidRequestException(message);
        }
    }
}