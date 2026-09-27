package org.example.ui;

import org.example.algorithm.SearchStep;
import org.example.model.CellType;
import org.example.model.Grid;
import org.example.model.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GridPanel extends JPanel {

    public enum EditMode {
        WALL,
        START,
        GOAL,
        ERASE
    }

    private final Grid grid;

    private EditMode editMode = EditMode.WALL;

    private Set<Position> open = Collections.emptySet();
    private Set<Position> closed = Collections.emptySet();
    private List<Position> path = Collections.emptyList();

    private Position current;

    private boolean editingEnabled = true;

    public GridPanel(Grid grid) {

        this.grid = grid;

        setPreferredSize(new Dimension(760, 650));
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                if (!editingEnabled) {
                    return;
                }

                Position position = positionFromMouse(e);

                if (position == null) {
                    return;
                }

                switch (editMode) {

                    case WALL ->
                            grid.toggleWall(position);

                    case START ->
                            grid.setStart(position);

                    case GOAL ->
                            grid.setGoal(position);

                    case ERASE ->
                            grid.setWall(position, false);
                }

                clearSearchVisualization();
                repaint();
            }
        });
    }

    private Position positionFromMouse(MouseEvent e) {

        int cellWidth = getWidth() / grid.getColumns();
        int cellHeight = getHeight() / grid.getRows();

        if (cellWidth <= 0 || cellHeight <= 0) {
            return null;
        }

        int column = e.getX() / cellWidth;
        int row = e.getY() / cellHeight;

        Position position = new Position(row, column);

        return grid.isValid(position)
                ? position
                : null;
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        int cellWidth = getWidth() / grid.getColumns();
        int cellHeight = getHeight() / grid.getRows();

        for (int row = 0; row < grid.getRows(); row++) {

            for (int column = 0;
                 column < grid.getColumns();
                 column++) {

                Position position =
                        new Position(row, column);

                int x = column * cellWidth;
                int y = row * cellHeight;

                g2.setColor(
                        getColorForPosition(position)
                );

                g2.fillRect(
                        x,
                        y,
                        cellWidth,
                        cellHeight
                );

                g2.setColor(new Color(210, 215, 220));

                g2.drawRect(
                        x,
                        y,
                        cellWidth,
                        cellHeight
                );

                drawSymbol(
                        g2,
                        position,
                        x,
                        y,
                        cellWidth,
                        cellHeight
                );
            }
        }

        g2.dispose();
    }

    private Color getColorForPosition(Position position) {

        CellType type = grid.getCell(position);

        if (type == CellType.WALL) {
            return new Color(44, 52, 63);
        }

        if (type == CellType.START) {
            return new Color(46, 204, 113);
        }

        if (type == CellType.GOAL) {
            return new Color(231, 76, 60);
        }

        if (path.contains(position)) {
            return new Color(52, 152, 219);
        }

        if (position.equals(current)) {
            return new Color(155, 89, 182);
        }

        if (closed.contains(position)) {
            return new Color(255, 190, 118);
        }

        if (open.contains(position)) {
            return new Color(255, 234, 167);
        }

        return new Color(248, 249, 250);
    }

    private void drawSymbol(
            Graphics2D g2,
            Position position,
            int x,
            int y,
            int width,
            int height
    ) {

        CellType type = grid.getCell(position);

        String text = null;

        if (type == CellType.START) {
            text = "A";
        } else if (type == CellType.GOAL) {
            text = "B";
        }

        if (text == null) {
            return;
        }

        g2.setColor(Color.WHITE);
        g2.setFont(
                getFont()
                        .deriveFont(
                                Font.BOLD,
                                Math.max(14f, height * 0.5f)
                        )
        );

        FontMetrics metrics = g2.getFontMetrics();

        int textX =
                x + (width - metrics.stringWidth(text)) / 2;

        int textY =
                y + (
                        height
                                - metrics.getHeight()
                ) / 2
                        + metrics.getAscent();

        g2.drawString(text, textX, textY);
    }

    public void showStep(SearchStep step) {

        this.open = step.getOpen();
        this.closed = step.getClosed();
        this.current = step.getCurrent();

        repaint();
    }

    public void showPath(List<Position> path) {

        this.path = path;
        this.current = null;

        repaint();
    }

    public void clearSearchVisualization() {

        open = Collections.emptySet();
        closed = Collections.emptySet();
        path = Collections.emptyList();
        current = null;

        repaint();
    }

    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

    public void setEditingEnabled(boolean editingEnabled) {
        this.editingEnabled = editingEnabled;
    }
}