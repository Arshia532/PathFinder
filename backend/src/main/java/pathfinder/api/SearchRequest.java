package pathfinder.api;

import java.util.List;

import pathfinder.grid.Position;

/**
 * The body of {@code POST /api/search}: the whole map plus the search settings.
 * The server keeps nothing between requests. Values are checked by the search
 * service, not here, so that errors can be reported as readable messages.
 *
 * @param width         the number of columns
 * @param height        the number of rows
 * @param start         the start cell
 * @param end           the end cell
 * @param walls         the wall cells, in any order; never null after
 *                      construction
 * @param algorithm     the algorithm to run
 * @param bidirectional whether to search from both ends at once
 */
public record SearchRequest(int width, int height, Position start, Position end,
        List<Position> walls, AlgorithmId algorithm, boolean bidirectional) {

    /**
     * Creates a request, treating missing walls as none and copying the list.
     *
     * @param width         the number of columns
     * @param height        the number of rows
     * @param start         the start cell
     * @param end           the end cell
     * @param walls         the wall cells, or null for none
     * @param algorithm     the algorithm to run
     * @param bidirectional whether to search from both ends at once
     * @throws NullPointerException if the walls list contains null
     */
    public SearchRequest {
        walls = walls == null ? List.of() : List.copyOf(walls);
    }
}