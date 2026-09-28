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

    private EditMode editMode =
            EditMode.WALL;

    private Set<Position> open =
            Collections.emptySet();

    private Set<Position> closed =
            Collections.emptySet();

    private List<Position> path =
            Collections.emptyList();

    private Position current;

    private SearchStep currentStep;

    private boolean editingEnabled = true;
    private boolean dragging = false;

    private Position lastDragPosition;

    public GridPanel(Grid grid) {

        this.grid = grid;

        setPreferredSize(
                new Dimension(800, 680)
        );

        setBackground(
                new Color(17, 24, 39)
        );

        /*
         * Necessário para tooltips dinâmicos.
         */
        ToolTipManager
                .sharedInstance()
                .registerComponent(this);

        configureMouseEditor();
    }

    private void configureMouseEditor() {

        MouseAdapter mouseEditor =
                new MouseAdapter() {

                    @Override
                    public void mousePressed(
                            MouseEvent e
                    ) {

                        if (!editingEnabled) {
                            return;
                        }

                        Position position =
                                positionFromMouse(e);

                        if (position == null) {
                            return;
                        }

                        if (editMode
                                == EditMode.START) {

                            grid.setStart(position);

                            clearSearchVisualization();

                            return;
                        }

                        if (editMode
                                == EditMode.GOAL) {

                            grid.setGoal(position);

                            clearSearchVisualization();

                            return;
                        }

                        dragging = true;
                        lastDragPosition = null;

                        applyDragEdit(position);
                    }

                    @Override
                    public void mouseDragged(
                            MouseEvent e
                    ) {

                        if (!editingEnabled
                                || !dragging) {

                            return;
                        }

                        Position position =
                                positionFromMouse(e);

                        if (position != null) {
                            applyDragEdit(position);
                        }
                    }

                    @Override
                    public void mouseReleased(
                            MouseEvent e
                    ) {

                        dragging = false;
                        lastDragPosition = null;
                    }

                    @Override
                    public void mouseExited(
                            MouseEvent e
                    ) {

                        dragging = false;
                        lastDragPosition = null;
                    }
                };

        addMouseListener(mouseEditor);
        addMouseMotionListener(mouseEditor);
    }

    private void applyDragEdit(
            Position position
    ) {

        if (position.equals(
                lastDragPosition
        )) {
            return;
        }

        lastDragPosition = position;

        if (editMode == EditMode.WALL) {

            grid.setWall(
                    position,
                    true
            );

        } else if (
                editMode == EditMode.ERASE
        ) {

            grid.setWall(
                    position,
                    false
            );
        }

        clearSearchVisualization();
    }

    private Position positionFromMouse(
            MouseEvent e
    ) {

        Insets insets = getInsets();

        int availableWidth =
                getWidth()
                        - insets.left
                        - insets.right;

        int availableHeight =
                getHeight()
                        - insets.top
                        - insets.bottom;

        int cellWidth =
                availableWidth
                        / grid.getColumns();

        int cellHeight =
                availableHeight
                        / grid.getRows();

        int cellSize =
                Math.min(
                        cellWidth,
                        cellHeight
                );

        if (cellSize <= 0) {
            return null;
        }

        int gridWidth =
                cellSize
                        * grid.getColumns();

        int gridHeight =
                cellSize
                        * grid.getRows();

        int offsetX =
                (getWidth() - gridWidth)
                        / 2;

        int offsetY =
                (getHeight() - gridHeight)
                        / 2;

        int mouseX =
                e.getX() - offsetX;

        int mouseY =
                e.getY() - offsetY;

        if (mouseX < 0
                || mouseY < 0
                || mouseX >= gridWidth
                || mouseY >= gridHeight) {

            return null;
        }

        int column =
                mouseX / cellSize;

        int row =
                mouseY / cellSize;

        Position position =
                new Position(row, column);

        return grid.isValid(position)
                ? position
                : null;
    }

    @Override
    public String getToolTipText(
            MouseEvent event
    ) {

        Position position =
                positionFromMouse(event);

        if (position == null) {
            return null;
        }

        StringBuilder tooltip =
                new StringBuilder(
                        "<html>"
                );

        tooltip.append("<b>Posição:</b> ")
                .append(position);

        CellType type =
                grid.getCell(position);

        tooltip.append(
                "<br><b>Tipo:</b> "
        ).append(type);

        if (currentStep != null) {

            Double g =
                    currentStep.getG(
                            position
                    );

            Double h =
                    currentStep.getH(
                            position
                    );

            Double f =
                    currentStep.getF(
                            position
                    );

            if (g != null) {

                tooltip.append(
                        "<br><b>g(n):</b> "
                ).append(
                        formatValue(g)
                );
            }

            if (h != null) {

                tooltip.append(
                        "<br><b>h(n):</b> "
                ).append(
                        formatValue(h)
                );
            }

            if (f != null) {

                tooltip.append(
                        "<br><b>f(n):</b> "
                ).append(
                        formatValue(f)
                );
            }

            if (position.equals(current)) {

                tooltip.append(
                        "<br><b>Estado:</b> ATUAL"
                );

            } else if (
                    closed.contains(position)
            ) {

                tooltip.append(
                        "<br><b>Estado:</b> CLOSED"
                );

            } else if (
                    open.contains(position)
            ) {

                tooltip.append(
                        "<br><b>Estado:</b> OPEN"
                );
            }
        }

        tooltip.append("</html>");

        return tooltip.toString();
    }

    private String formatValue(
            double value
    ) {

        if (value == Math.rint(value)) {
            return String.format(
                    "%.0f",
                    value
            );
        }

        return String.format(
                "%.2f",
                value
        );
    }

    @Override
    protected void paintComponent(
            Graphics g
    ) {

        super.paintComponent(g);

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int cellWidth =
                getWidth()
                        / grid.getColumns();

        int cellHeight =
                getHeight()
                        / grid.getRows();

        int cellSize =
                Math.min(
                        cellWidth,
                        cellHeight
                );

        int gridWidth =
                cellSize
                        * grid.getColumns();

        int gridHeight =
                cellSize
                        * grid.getRows();

        int offsetX =
                (getWidth() - gridWidth)
                        / 2;

        int offsetY =
                (getHeight() - gridHeight)
                        / 2;

        for (int row = 0;
             row < grid.getRows();
             row++) {

            for (int column = 0;
                 column < grid.getColumns();
                 column++) {

                Position position =
                        new Position(
                                row,
                                column
                        );

                int x =
                        offsetX
                                + column
                                * cellSize;

                int y =
                        offsetY
                                + row
                                * cellSize;

                drawCell(
                        g2,
                        position,
                        x,
                        y,
                        cellSize
                );
            }
        }

        g2.dispose();
    }

    private void drawCell(
            Graphics2D g2,
            Position position,
            int x,
            int y,
            int size
    ) {

        int gap = 2;

        g2.setColor(
                getColorForPosition(
                        position
                )
        );

        g2.fillRoundRect(
                x + gap,
                y + gap,
                size - gap * 2,
                size - gap * 2,
                6,
                6
        );

        drawSymbol(
                g2,
                position,
                x,
                y,
                size
        );
    }

    private Color getColorForPosition(
            Position position
    ) {

        CellType type =
                grid.getCell(position);

        if (type == CellType.WALL) {
            return new Color(
                    55,
                    65,
                    81
            );
        }

        if (type == CellType.START) {
            return new Color(
                    16,
                    185,
                    129
            );
        }

        if (type == CellType.GOAL) {
            return new Color(
                    239,
                    68,
                    68
            );
        }

        if (path.contains(position)) {
            return new Color(
                    59,
                    130,
                    246
            );
        }

        if (position.equals(current)) {
            return new Color(
                    168,
                    85,
                    247
            );
        }

        if (closed.contains(position)) {
            return new Color(
                    249,
                    115,
                    22
            );
        }

        if (open.contains(position)) {
            return new Color(
                    250,
                    204,
                    21
            );
        }

        return new Color(
                31,
                41,
                55
        );
    }

    private void drawSymbol(
            Graphics2D g2,
            Position position,
            int x,
            int y,
            int size
    ) {

        CellType type =
                grid.getCell(position);

        String text = null;

        if (type == CellType.START) {
            text = "A";
        } else if (
                type == CellType.GOAL
        ) {
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
                                Math.max(
                                        13f,
                                        size * 0.48f
                                )
                        )
        );

        FontMetrics metrics =
                g2.getFontMetrics();

        int textX =
                x
                        + (
                        size
                                - metrics
                                .stringWidth(text)
                ) / 2;

        int textY =
                y
                        + (
                        size
                                - metrics
                                .getHeight()
                ) / 2
                        + metrics
                        .getAscent();

        g2.drawString(
                text,
                textX,
                textY
        );
    }

    public void showStep(
            SearchStep step
    ) {

        currentStep = step;

        open = step.getOpen();
        closed = step.getClosed();
        current = step.getCurrent();

        repaint();
    }

    public void showPath(
            List<Position> path
    ) {

        this.path = path;
        current = null;

        repaint();
    }

    public void clearSearchVisualization() {

        open = Collections.emptySet();
        closed = Collections.emptySet();
        path = Collections.emptyList();

        current = null;
        currentStep = null;

        repaint();
    }

    public void setEditMode(
            EditMode editMode
    ) {

        this.editMode = editMode;
    }

    public EditMode getEditMode() {
        return editMode;
    }

    public void setEditingEnabled(
            boolean editingEnabled
    ) {

        this.editingEnabled =
                editingEnabled;

        if (!editingEnabled) {

            dragging = false;
            lastDragPosition = null;
        }
    }
}