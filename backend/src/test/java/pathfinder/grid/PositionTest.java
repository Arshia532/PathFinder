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
    void rejectsNegativeCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new Position(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Position(0, -1));
    }
}
