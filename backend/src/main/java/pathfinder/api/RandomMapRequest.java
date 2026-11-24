package pathfinder.api;

/**
 * The body of {@code POST /api/maps/random} and {@code POST /api/maps/maze}.
 *
 * @param width  the number of columns
 * @param height the number of rows
 * @param seed   the seed; the same seed and size always give the same map
 */
public record RandomMapRequest(int width, int height, long seed) {
}