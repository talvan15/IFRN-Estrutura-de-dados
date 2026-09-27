package org.example.ui;

import org.example.algorithm.*;
import org.example.model.Grid;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainWindow extends JFrame {

    private final Grid grid;
    private final GridPanel gridPanel;

    private final JComboBox<String> algorithmCombo;

    private final JLabel statusLabel;
    private final JLabel nodesLabel;
    private final JLabel costLabel;
    private final JLabel timeLabel;

    private final JSlider speedSlider;

    private Timer animationTimer;

    private SearchResult lastAStarResult;
    private SearchResult lastGreedyResult;

    public MainWindow() {

        super("Predator Pathfinding - A* vs Greedy");

        grid = new Grid(20, 25);
        grid.createDefaultMap();

        gridPanel = new GridPanel(grid);

        algorithmCombo =
                new JComboBox<>(
                        new String[]{
                                "A*",
                                "Greedy Best-First"
                        }
                );

        statusLabel = new JLabel("Pronto");
        nodesLabel = new JLabel("Nós explorados: -");
        costLabel = new JLabel("Custo: -");
        timeLabel = new JLabel("Tempo: -");

        speedSlider = new JSlider(20, 500, 120);

        configureWindow();
    }

    private void configureWindow() {

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLayout(new BorderLayout());

        add(gridPanel, BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.EAST);

        pack();

        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);
    }

    private JPanel createControlPanel() {

        JPanel panel = new JPanel();

        panel.setPreferredSize(new Dimension(280, 650));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        JLabel title =
                new JLabel("PATHFINDING LAB");

        title.setFont(
                title.getFont().deriveFont(
                        Font.BOLD,
                        20f
                )
        );

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        panel.add(new JLabel("Algoritmo"));
        panel.add(algorithmCombo);

        panel.add(Box.createVerticalStrut(20));

        JButton runButton =
                new JButton("▶ Executar");

        runButton.addActionListener(
                e -> executeSearch()
        );

        panel.add(runButton);

        panel.add(Box.createVerticalStrut(10));

        JButton resetButton =
                new JButton("↻ Mapa padrão");

        resetButton.addActionListener(e -> {

            stopAnimation();

            grid.createDefaultMap();
            gridPanel.clearSearchVisualization();

            resetMetrics();
        });

        panel.add(resetButton);

        panel.add(Box.createVerticalStrut(10));

        JButton clearButton =
                new JButton("Limpar mapa");

        clearButton.addActionListener(e -> {

            stopAnimation();

            grid.clear();
            gridPanel.clearSearchVisualization();

            resetMetrics();
        });

        panel.add(clearButton);

        panel.add(Box.createVerticalStrut(25));

        panel.add(new JLabel("Editor"));

        JButton wallButton =
                new JButton("🧱 Obstáculo");

        JButton startButton =
                new JButton("A - Origem");

        JButton goalButton =
                new JButton("B - Destino");

        JButton eraseButton =
                new JButton("Borracha");

        wallButton.addActionListener(
                e -> gridPanel.setEditMode(
                        GridPanel.EditMode.WALL
                )
        );

        startButton.addActionListener(
                e -> gridPanel.setEditMode(
                        GridPanel.EditMode.START
                )
        );

        goalButton.addActionListener(
                e -> gridPanel.setEditMode(
                        GridPanel.EditMode.GOAL
                )
        );

        eraseButton.addActionListener(
                e -> gridPanel.setEditMode(
                        GridPanel.EditMode.ERASE
                )
        );

        panel.add(wallButton);
        panel.add(startButton);
        panel.add(goalButton);
        panel.add(eraseButton);

        panel.add(Box.createVerticalStrut(25));

        panel.add(new JLabel("Velocidade da animação"));
        panel.add(speedSlider);

        panel.add(Box.createVerticalStrut(25));

        JLabel metricsTitle =
                new JLabel("Métricas");

        metricsTitle.setFont(
                metricsTitle.getFont()
                        .deriveFont(Font.BOLD)
        );

        panel.add(metricsTitle);
        panel.add(Box.createVerticalStrut(8));

        panel.add(statusLabel);
        panel.add(nodesLabel);
        panel.add(costLabel);
        panel.add(timeLabel);

        panel.add(Box.createVerticalStrut(20));

        JButton comparisonButton =
                new JButton("Comparar algoritmos");

        comparisonButton.addActionListener(
                e -> showComparison()
        );

        panel.add(comparisonButton);

        return panel;
    }

    private void executeSearch() {

        stopAnimation();

        if (grid.getStart() == null
                || grid.getGoal() == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Defina os pontos A e B antes de executar.",
                    "Mapa incompleto",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        gridPanel.clearSearchVisualization();
        gridPanel.setEditingEnabled(false);

        SearchAlgorithm algorithm;

        if (algorithmCombo.getSelectedIndex() == 0) {
            algorithm = new AStarSearch();
        } else {
            algorithm = new GreedySearch();
        }

        SearchResult result =
                algorithm.search(grid);

        if (algorithm instanceof AStarSearch) {
            lastAStarResult = result;
        } else {
            lastGreedyResult = result;
        }

        animateResult(result);
    }

    private void animateResult(SearchResult result) {

        if (result.getSteps().isEmpty()) {
            finishAnimation(result);
            return;
        }

        final int[] index = {0};

        animationTimer =
                new Timer(
                        speedSlider.getValue(),
                        null
                );

        animationTimer.addActionListener(e -> {

            if (index[0] < result.getSteps().size()) {

                SearchStep step =
                        result
                                .getSteps()
                                .get(index[0]);

                gridPanel.showStep(step);

                statusLabel.setText(
                        "Explorando... passo "
                                + (index[0] + 1)
                );

                nodesLabel.setText(
                        "Nós explorados: "
                                + step.getClosed().size()
                );

                index[0]++;

            } else {

                stopAnimation();

                gridPanel.showPath(
                        result.getPath()
                );

                finishAnimation(result);
            }
        });

        animationTimer.start();
    }

    private void finishAnimation(SearchResult result) {

        gridPanel.setEditingEnabled(true);

        if (result.isFound()) {

            statusLabel.setText(
                    "Caminho encontrado - "
                            + result.getAlgorithmName()
            );

        } else {

            statusLabel.setText(
                    "Nenhum caminho encontrado"
            );
        }

        nodesLabel.setText(
                "Nós explorados: "
                        + result.getExploredNodes()
        );

        costLabel.setText(
                String.format(
                        "Custo: %.0f",
                        result.getPathCost()
                )
        );

        timeLabel.setText(
                String.format(
                        "Tempo: %.3f ms",
                        result.getExecutionTimeMs()
                )
        );
    }

    private void showComparison() {

        if (grid.getStart() == null
                || grid.getGoal() == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Defina os pontos A e B primeiro."
            );

            return;
        }

        /*
         * Executamos novamente para comparar
         * exatamente o mapa atual.
         */
        lastAStarResult =
                new AStarSearch().search(grid);

        lastGreedyResult =
                new GreedySearch().search(grid);

        String comparison = String.format(
                """
                COMPARAÇÃO

                A*
                Nós explorados: %d
                Custo: %.0f
                Tempo: %.3f ms
                Caminho encontrado: %s

                GREEDY BEST-FIRST
                Nós explorados: %d
                Custo: %.0f
                Tempo: %.3f ms
                Caminho encontrado: %s
                """,
                lastAStarResult.getExploredNodes(),
                lastAStarResult.getPathCost(),
                lastAStarResult.getExecutionTimeMs(),
                lastAStarResult.isFound() ? "Sim" : "Não",

                lastGreedyResult.getExploredNodes(),
                lastGreedyResult.getPathCost(),
                lastGreedyResult.getExecutionTimeMs(),
                lastGreedyResult.isFound() ? "Sim" : "Não"
        );

        JOptionPane.showMessageDialog(
                this,
                comparison,
                "Comparação A* x Greedy",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void stopAnimation() {

        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }

        gridPanel.setEditingEnabled(true);
    }

    private void resetMetrics() {

        statusLabel.setText("Pronto");
        nodesLabel.setText("Nós explorados: -");
        costLabel.setText("Custo: -");
        timeLabel.setText("Tempo: -");

        lastAStarResult = null;
        lastGreedyResult = null;
    }
}