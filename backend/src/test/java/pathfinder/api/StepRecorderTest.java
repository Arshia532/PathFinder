package pathfinder.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import pathfinder.grid.Grid;
import pathfinder.grid.Position;
import pathfinder.search.BreadthFirstSearch;
import pathfinder.search.DepthFirstSearch;
import pathfinder.search.SearchResult;

class StepRecorderTest {

    @Test
    void groupsDiscoveriesUnderTheExpandedCell() {
        Grid grid = new Grid(2, 2);
        StepRecorder recorder = new StepRecorder(grid, SearchSide.FORWARD);

        recorder.cellDiscovered(0); // start
        recorder.cellExpanded(0);
        recorder.cellDiscovered(1);
        recorder.cellDiscovered(2);
        recorder.cellExpanded(1);

        assertEquals(List.of(
                new SearchStep(new Position(0, 0),
                        List.of(new Position(0, 1), new Position(1, 0)), SearchSide.FORWARD),
                new SearchStep(new Position(0, 1), List.of(), SearchSide.FORWARD)),
                recorder.steps());
    }

    @Test
    void recordsOneStepPerExpansionEndingAtTheGoal() {
        Grid grid = new Grid(1, 3);
        StepRecorder recorder = new StepRecorder(grid, SearchSide.FORWARD);

        SearchResult result = new BreadthFirstSearch()
                .search(grid, new Position(0, 0), new Position(0, 2), recorder);
        List<SearchStep> steps = recorder.steps();

        assertEquals(result.expandedCount(), steps.size());
        assertEquals(new Position(0, 2), steps.getLast().expanded());
        assertEquals(List.of(), steps.getLast().discovered());
    }

    @Test
    void discoversEachCellAtMostOnce() {
        Grid grid = new Grid(5, 5);
        StepRecorder recorder = new StepRecorder(grid, SearchSide.FORWARD);

        new DepthFirstSearch().search(grid, new Position(0, 0), new Position(4, 4), recorder);

        Set<Position> seen = new HashSet<>();
        for (SearchStep step : recorder.steps()) {
            for (Position cell : step.discovered()) {
                assertTrue(seen.add(cell), "discovered twice: " + cell);
            }
        }
    }
}