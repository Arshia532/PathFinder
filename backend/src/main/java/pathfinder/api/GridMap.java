package pathfinder.api;

import java.util.List;

import pathfinder.grid.Position;

/**
 * A map without search settings; the shape of a saved map file.
 *
 * @param width  the number of columns
 * @param height the number of rows
 * @param start  the start cell
 * @param end    the end cell
 * @param walls  the wall cells
 */
public record GridMap(int width, int height, Position start, Position end, List<Position> walls) {

    /**
     * Creates a map, treating missing walls as none and copying the list.
     *
     * @param width  the number of columns
     * @param height the number of rows
     * @param start  the start cell
     * @param end    the end cell
     * @param walls  the wall cells, or null for none
     */
    public GridMap {
        walls = walls == null ? List.of() : List.copyOf(walls);
    }
}