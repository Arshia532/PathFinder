package pathfinder.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pathfinder.generation.MazeGenerator;
import pathfinder.generation.RandomGridGenerator;
import pathfinder.grid.Grid;
import pathfinder.grid.Position;
import pathfinder.search.BreadthFirstSearch;
import pathfinder.search.DepthFirstSearch;
import pathfinder.search.SearchAlgorithm;
import pathfinder.search.SearchObserver;
import pathfinder.search.SearchResult;

final class PathFinderApi {
    private static final int MIN_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final double WALL_PROBABILITY = 0.30;

    private PathFinderApi() {
    }

    static MapResponse generate(GenerateRequest request) {
        validateSize(request.width(), request.height());
        long seed = parseSeed(request.seed());
        Grid grid;
        Position start;
        Position end;
        if ("maze".equals(request.mode())) {
            MazeGenerator.Maze maze = new MazeGenerator().generate(request.height(), request.width(), seed);
            grid = maze.grid();
            start = maze.start();
            end = maze.destination();
        } else if ("random".equals(request.mode())) {
            grid = new RandomGridGenerator().generate(request.height(), request.width(), seed, WALL_PROBABILITY);
            start = new Position(1, 1);
            end = new Position(request.height() - 2, request.width() - 2);
        } else {
            throw new IllegalArgumentException("Choose maze or random generation.");
        }

        int startIndex = grid.indexOf(start);
        int endIndex = grid.indexOf(end);
        List<Position> walls = new ArrayList<>();
        for (int index = 0; index < grid.size(); index++) {
            if (grid.isBlocked(index) && index != startIndex && index != endIndex) {
                walls.add(grid.positionAt(index));
            }
        }
        return new MapResponse(grid.columns(), grid.rows(), start, end, walls);
    }

    static SearchResponse search(SearchRequest request) {
        validateSize(request.width(), request.height());
        if (request.bidirectional()) {
            throw new IllegalArgumentException("Bidirectional search is not available yet.");
        }
        SearchAlgorithm algorithm = switch (request.algorithm() == null ? "" : request.algorithm()) {
            case "bfs" -> new BreadthFirstSearch();
            case "dfs" -> new DepthFirstSearch();
            default -> throw new IllegalArgumentException("Choose BFS or DFS.");
        };

        Position start = validateCell(request.start(), request.width(), request.height());
        Position end = validateCell(request.end(), request.width(), request.height());
        if (request.walls() == null || request.walls().size() > request.width() * request.height()) {
            throw new IllegalArgumentException("Provide a wall list with at most one entry per cell.");
        }
        boolean[] walls = new boolean[request.width() * request.height()];
        for (Cell cell : request.walls()) {
            Position wall = validateCell(cell, request.width(), request.height());
            walls[wall.row() * request.width() + wall.column()] = true;
        }
        int[] costs = new int[walls.length];
        Arrays.fill(costs, 1);
        Grid grid = new Grid(request.height(), request.width(), walls, costs);

        List<SearchStep> steps = new ArrayList<>();
        SearchObserver observer = new SearchObserver() {
            @Override
            public void cellExpanded(int cell) {
                steps.add(new SearchStep(grid.positionAt(cell), new ArrayList<>(), "forward"));
            }

            @Override
            public void cellDiscovered(int cell) {
                // The start is discovered before the first expansion.
                if (!steps.isEmpty()) {
                    steps.getLast().discovered().add(grid.positionAt(cell));
                }
            }
        };
        SearchResult result = algorithm.search(grid, start, end, observer);
        return new SearchResponse(result.found(), steps, result.path(),
                new SearchStats(result.expandedCount(), result.found() ? result.moves() : null,
                        result.found() ? result.cost() : null));
    }

    private static long parseSeed(String seed) {
        try {
            return Long.parseLong(seed);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a whole number from -9223372036854775808 to 9223372036854775807.");
        }
    }

    private static void validateSize(int width, int height) {
        if (width < MIN_SIZE || width > MAX_SIZE || height < MIN_SIZE || height > MAX_SIZE) {
            throw new IllegalArgumentException("Rows and columns must be between 10 and 50.");
        }
    }

    private static Position validateCell(Cell cell, int width, int height) {
        if (cell == null || cell.row() == null || cell.column() == null
                || cell.row() < 0 || cell.row() >= height || cell.column() < 0 || cell.column() >= width) {
            throw new IllegalArgumentException("Every cell must have a row and column within the grid.");
        }
        return new Position(cell.row(), cell.column());
    }

    record GenerateRequest(int width, int height, String seed, String mode) {
    }

    record Cell(Integer row, Integer column) {
    }

    record MapResponse(int width, int height, Position start, Position end, List<Position> walls) {
    }

    record SearchRequest(int width, int height, Cell start, Cell end, List<Cell> walls,
                         String algorithm, boolean bidirectional) {
    }

    record SearchStep(Position expanded, List<Position> discovered, String side) {
    }

    record SearchStats(int visited, Integer pathLength, Long pathCost) {
    }

    record SearchResponse(boolean found, List<SearchStep> steps, List<Position> path, SearchStats stats) {
    }
}
