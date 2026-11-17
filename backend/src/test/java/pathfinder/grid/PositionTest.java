package pathfinder.grid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionTest {
    @Test
    void positionValues() {
        Position position = new Position(2, 3);
        Position equalPosition = new Position(2, 3);
        assertEquals(position, equalPosition);
        assertEquals(position.hashCode(), equalPosition.hashCode());
        assertNotEquals(position, new Position(3, 3));
        assertNotEquals(position, new Position(2, 4));
    }

    @Test
    void exposesItsCoordinatesAndAllowsTheOrigin() {
        Position position = new Position(4, 7);
        assertEquals(4, position.row());
        assertEquals(7, position.column());
        assertEquals(new Position(0, 0), new Position(0, 0));
        assertNotEquals(new Position(1, 2), new Position(2, 1), "Row and column are not interchangeable");
    }

    @Test
    void rejectsNegativeCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new Position(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Position(0, -1));
        assertThrows(IllegalArgumentException.class, () -> new Position(-1, -1));
        assertThrows(IllegalArgumentException.class, () -> new Position(Integer.MIN_VALUE, 0));
    }
}
