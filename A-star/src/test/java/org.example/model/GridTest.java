package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GridTest {

    private Grid grid;

    @BeforeEach
    void setUp() {
        grid = new Grid(5, 5);
    }

    @Test
    void newGridShouldBeEmpty() {

        for (int row = 0; row < 5; row++) {

            for (int column = 0; column < 5; column++) {

                Position position =
                        new Position(row, column);

                assertEquals(
                        CellType.EMPTY,
                        grid.getCell(position)
                );
            }
        }
    }

    @Test
    void shouldDefineStartPosition() {

        Position start = new Position(1, 1);

        grid.setStart(start);

        assertEquals(start, grid.getStart());

        assertEquals(
                CellType.START,
                grid.getCell(start)
        );
    }

    @Test
    void shouldDefineGoalPosition() {

        Position goal = new Position(4, 4);

        grid.setGoal(goal);

        assertEquals(goal, grid.getGoal());

        assertEquals(
                CellType.GOAL,
                grid.getCell(goal)
        );
    }

    @Test
    void shouldCreateWall() {

        Position wall = new Position(2, 2);

        grid.setWall(wall, true);

        assertEquals(
                CellType.WALL,
                grid.getCell(wall)
        );

        assertFalse(grid.isWalkable(wall));
    }

    @Test
    void shouldRemoveWall() {

        Position position = new Position(2, 2);

        grid.setWall(position, true);
        grid.setWall(position, false);

        assertEquals(
                CellType.EMPTY,
                grid.getCell(position)
        );

        assertTrue(grid.isWalkable(position));
    }

    @Test
    void shouldNotCreateWallOverStart() {

        Position start = new Position(1, 1);

        grid.setStart(start);
        grid.setWall(start, true);

        assertEquals(
                CellType.START,
                grid.getCell(start)
        );
    }

    @Test
    void shouldNotCreateWallOverGoal() {

        Position goal = new Position(4, 4);

        grid.setGoal(goal);
        grid.setWall(goal, true);

        assertEquals(
                CellType.GOAL,
                grid.getCell(goal)
        );
    }

    @Test
    void shouldValidateGridBoundaries() {

        assertTrue(
                grid.isValid(new Position(0, 0))
        );

        assertTrue(
                grid.isValid(new Position(4, 4))
        );

        assertFalse(
                grid.isValid(new Position(-1, 0))
        );

        assertFalse(
                grid.isValid(new Position(0, -1))
        );

        assertFalse(
                grid.isValid(new Position(5, 0))
        );

        assertFalse(
                grid.isValid(new Position(0, 5))
        );
    }

    @Test
    void centerPositionShouldHaveFourNeighbors() {

        Position center = new Position(2, 2);

        List<Position> neighbors =
                grid.getNeighbors(center);

        assertEquals(4, neighbors.size());

        assertTrue(
                neighbors.contains(new Position(1, 2))
        );

        assertTrue(
                neighbors.contains(new Position(3, 2))
        );

        assertTrue(
                neighbors.contains(new Position(2, 1))
        );

        assertTrue(
                neighbors.contains(new Position(2, 3))
        );
    }

    @Test
    void cornerShouldHaveOnlyTwoNeighbors() {

        Position corner = new Position(0, 0);

        List<Position> neighbors =
                grid.getNeighbors(corner);

        assertEquals(2, neighbors.size());

        assertTrue(
                neighbors.contains(new Position(1, 0))
        );

        assertTrue(
                neighbors.contains(new Position(0, 1))
        );
    }

    @Test
    void wallShouldNotBeReturnedAsNeighbor() {

        Position center = new Position(2, 2);

        grid.setWall(
                new Position(2, 3),
                true
        );

        List<Position> neighbors =
                grid.getNeighbors(center);

        assertFalse(
                neighbors.contains(
                        new Position(2, 3)
                )
        );

        assertEquals(3, neighbors.size());
    }

    @Test
    void clearShouldRemoveEverything() {

        grid.setStart(new Position(0, 0));
        grid.setGoal(new Position(4, 4));
        grid.setWall(new Position(2, 2), true);

        grid.clear();

        assertNull(grid.getStart());
        assertNull(grid.getGoal());

        assertEquals(
                CellType.EMPTY,
                grid.getCell(new Position(2, 2))
        );
    }
}