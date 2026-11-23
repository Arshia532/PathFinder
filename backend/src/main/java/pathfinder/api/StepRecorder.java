package pathfinder.api;

import java.util.ArrayList;
import java.util.List;

import pathfinder.grid.Grid;
import pathfinder.grid.Position;
import pathfinder.search.SearchObserver;

/** Builds the playback steps of one search from its observer events. */
final class StepRecorder implements SearchObserver {
    private final Grid grid;
    private final SearchSide side;
    private final List<SearchStep> steps = new ArrayList<>();
    private final List<Position> discovered = new ArrayList<>();
    private Position expanded;

    StepRecorder(Grid grid, SearchSide side) {
        this.grid = grid;
        this.side = side;
    }

    @Override
    public void cellExpanded(int cell) {
        finishStep();
        expanded = grid.positionAt(cell);
    }

    @Override
    public void cellDiscovered(int cell) {
        // The start is discovered before any expansion; it is the initial frontier, not
        // a step
        if (expanded != null) {
            discovered.add(grid.positionAt(cell));
        }
    }

    /**
     * Returns the recorded steps; call after the search has finished.
     *
     * @return one step per expanded cell, in order
     */
    List<SearchStep> steps() {
        finishStep();
        return List.copyOf(steps);
    }

    private void finishStep() {
        if (expanded != null) {
            steps.add(new SearchStep(expanded, discovered, side));
            discovered.clear();
            expanded = null;
        }
    }
}