package pathfinder.api;

/**
 * Summary numbers for one search.
 *
 * @param visited    the number of expanded cells, equal to the number of steps
 * @param pathLength the number of moves along the path, or null when no path
 *                   exists
 * @param pathCost   the sum of the entry costs along the path, or null when no
 *                   path exists
 */
public record SearchStats(int visited, Integer pathLength, Long pathCost) {
}