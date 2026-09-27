package org.example.ui;

import javax.swing.*;
import java.awt.*;

public class ScrollablePanel extends JPanel
        implements Scrollable {

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(
            Rectangle visibleRect,
            int orientation,
            int direction
    ) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(
            Rectangle visibleRect,
            int orientation,
            int direction
    ) {
        return 80;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {

        /*
         * O painel sempre acompanha a largura
         * disponível no JScrollPane.
         *
         * Isso impede que os controles sejam
         * cortados horizontalmente.
         */
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}