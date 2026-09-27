package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Grid {

    private final int rows;
    private final int columns;

    private final CellType[][] cells;

    private Position start;
    private Position goal;

    public Grid(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.cells = new CellType[rows][columns];

        clear();
    }

    public void clear() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                cells[row][column] = CellType.EMPTY;
            }
        }

        start = null;
        goal = null;
    }

    public void createDefaultMap() {
        clear();

        setStart(new Position(2, 2));
        setGoal(new Position(rows - 3, columns - 3));

        int middleColumn = columns / 2;

        for (int row = 2; row < rows - 2; row++) {
            if (row != rows / 2) {
                setWall(new Position(row, middleColumn), true);
            }
        }

        for (int column = 4; column < columns - 4; column++) {
            if (column != middleColumn + 3) {
                setWall(new Position(rows / 3, column), true);
            }
        }
    }

    public boolean isValid(Position position) {
        return position.getRow() >= 0
                && position.getRow() < rows
                && position.getColumn() >= 0
                && position.getColumn() < columns;
    }

    public boolean isWalkable(Position position) {
        return isValid(position)
                && getCell(position) != CellType.WALL;
    }

    public CellType getCell(Position position) {
        return cells[position.getRow()][position.getColumn()];
    }

    public void setWall(Position position, boolean wall) {
        if (!isValid(position)) {
            return;
        }

        if (position.equals(start) || position.equals(goal)) {
            return;
        }

        cells[position.getRow()][position.getColumn()] =
                wall ? CellType.WALL : CellType.EMPTY;
    }

    public void toggleWall(Position position) {
        if (!isValid(position)) {
            return;
        }

        if (position.equals(start) || position.equals(goal)) {
            return;
        }

        boolean isWall = getCell(position) == CellType.WALL;

        setWall(position, !isWall);
    }

    public void setStart(Position position) {
        if (!isValid(position)) {
            return;
        }

        if (start != null) {
            cells[start.getRow()][start.getColumn()] = CellType.EMPTY;
        }

        if (position.equals(goal)) {
            goal = null;
        }

        start = position;
        cells[position.getRow()][position.getColumn()] = CellType.START;
    }

    public void setGoal(Position position) {
        if (!isValid(position)) {
            return;
        }

        if (goal != null) {
            cells[goal.getRow()][goal.getColumn()] = CellType.EMPTY;
        }

        if (position.equals(start)) {
            start = null;
        }

        goal = position;
        cells[position.getRow()][position.getColumn()] = CellType.GOAL;
    }

    public List<Position> getNeighbors(Position position) {

        List<Position> neighbors = new ArrayList<>();

        int row = position.getRow();
        int column = position.getColumn();

        Position[] candidates = {
                new Position(row - 1, column),
                new Position(row + 1, column),
                new Position(row, column - 1),
                new Position(row, column + 1)
        };

        for (Position candidate : candidates) {
            if (isWalkable(candidate)) {
                neighbors.add(candidate);
            }
        }

        return neighbors;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Position getStart() {
        return start;
    }

    public Position getGoal() {
        return goal;
    }
}