package pathfinder.api;

/**
 * The body of every 4xx and 5xx response.
 *
 * @param message a sentence the user interface can show as-is
 */
public record ApiError(String message) {
}