package org.example.ui;

import org.example.model.Graph;

import javax.swing.JFrame;
import java.awt.Dimension;

/** Hosts the graph editor and algorithm results. */
@SuppressWarnings("serial")
public final class MainFrame extends JFrame {
    public MainFrame() {
        this(WorkspacePanel.circuitPreset());
    }

    public MainFrame(Graph initialGraph) {
        super("Caixeiro Viajante");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new WorkspacePanel(initialGraph));
        setMinimumSize(new Dimension(980, 590));
        setSize(1180, 730);
        setLocationRelativeTo(null);
    }
}
