package org.example.model;

import org.example.model.Position;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    void shouldStoreRowAndColumn() {

        Position position = new Position(3, 7);

        assertEquals(3, position.getRow());
        assertEquals(7, position.getColumn());
    }

    @Test
    void positionsWithSameCoordinatesShouldBeEqual() {

        Position first = new Position(2, 5);
        Position second = new Position(2, 5);

        assertEquals(first, second);
    }

    @Test
    void positionsWithDifferentCoordinatesShouldNotBeEqual() {

        Position first = new Position(2, 5);
        Position second = new Position(3, 5);

        assertNotEquals(first, second);
    }

    @Test
    void equalPositionsShouldHaveSameHashCode() {

        Position first = new Position(4, 8);
        Position second = new Position(4, 8);

        assertEquals(
                first.hashCode(),
                second.hashCode()
        );
    }

    @Test
    void hashSetShouldRecognizeEquivalentPosition() {

        Set<Position> visited = new HashSet<>();

        visited.add(new Position(2, 5));

        assertTrue(
                visited.contains(new Position(2, 5))
        );
    }
}