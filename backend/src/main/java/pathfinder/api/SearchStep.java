package pathfinder.api;

import java.util.List;

import pathfinder.grid.Position;

/**
 * One expansion during a search: the cell taken from the frontier, and the
 * cells that entered the frontier for the first time while it was examined.
 *
 * @param expanded   the expanded cell
 * @param discovered the newly discovered cells, possibly empty
 * @param side       which search produced this step
 */
public record SearchStep(Position expanded, List<Position> discovered, SearchSide side) {

    /**
     * Creates a step, storing an unmodifiable copy of the discovered cells.
     *
     * @param expanded   the expanded cell
     * @param discovered the newly discovered cells, possibly empty
     * @param side       which search produced this step
     * @throws NullPointerException if the list or any cell in it is null
     */
    public SearchStep {
        discovered = List.copyOf(discovered);
    }
}