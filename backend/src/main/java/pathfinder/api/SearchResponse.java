package pathfinder.api;

import java.util.List;

import pathfinder.grid.Position;

/**
 * The response of {@code POST /api/search}. An unreachable end is a normal
 * result ({@code found} is false), not an error.
 *
 * @param found whether a path was found
 * @param steps every expansion, in order
 * @param path  the cells from start to end, both included, or empty when not
 *              found
 * @param stats summary numbers
 */
public record SearchResponse(boolean found, List<SearchStep> steps, List<Position> path,
        SearchStats stats) {

    /**
     * Creates a response, storing unmodifiable copies of the lists.
     *
     * @param found whether a path was found
     * @param steps every expansion, in order
     * @param path  the cells from start to end, or empty when not found
     * @param stats summary numbers
     * @throws NullPointerException if a list or any element in it is null
     */
    public SearchResponse {
        steps = List.copyOf(steps);
        path = List.copyOf(path);
    }
}