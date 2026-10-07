package org.example.ui;

import org.example.algorithm.AStarSearch;
import org.example.algorithm.GreedySearch;
import org.example.algorithm.SearchAlgorithm;
import org.example.algorithm.SearchResult;
import org.example.algorithm.SearchStep;
import org.example.model.Grid;
import org.example.model.Position;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MainWindow extends JFrame {


    private static final Color BACKGROUND =
            new Color(11, 18, 32);

    private static final Color PANEL =
            new Color(17, 24, 39);

    private static final Color CARD =
            new Color(31, 41, 55);

    private static final Color BORDER =
            new Color(55, 65, 81);

    private static final Color TEXT =
            new Color(243, 244, 246);

    private static final Color MUTED =
            new Color(156, 163, 175);

    private static final Color ACCENT =
            new Color(59, 130, 246);

    private final Grid grid;

    private final GridPanel gridPanel;


    private JComboBox<String> algorithmCombo;

    private JLabel statusLabel;
    private JLabel nodesLabel;
    private JLabel costLabel;
    private JLabel timeLabel;

    /*
     * Comparação A*.
     */
    private JLabel aStarNodesLabel;
    private JLabel aStarCostLabel;
    private JLabel aStarTimeLabel;

    /*
     * Comparação Greedy.
     */
    private JLabel greedyNodesLabel;
    private JLabel greedyCostLabel;
    private JLabel greedyTimeLabel;

    /*
     * Sliders.
     */
    private JSlider speedSlider;

    /*
     * Botões do editor.
     */
    private JButton wallButton;
    private JButton eraseButton;
    private JButton startButton;
    private JButton goalButton;

    private Timer animationTimer;

    private SearchResult lastAStarResult;
    private SearchResult lastGreedyResult;



    public MainWindow() {

        super(
                "Pathfinding Lab - A* vs Greedy"
        );

        grid =
                new Grid(
                        20,
                        25
                );

        grid.createDefaultMap();

        gridPanel =
                new GridPanel(grid);

        configureWindow();
    }


    private void configureWindow() {

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        getContentPane()
                .setBackground(BACKGROUND);

        setLayout(
                new BorderLayout(
                        16,
                        16
                )
        );

        /*
         * Cabeçalho.
         */
        JPanel header =
                createHeader();

        /*
         * Área central contendo o mapa.
         */
        JPanel center =
                createGridContainer();

        /*
         * Painel lateral.
         */
        JScrollPane controlScrollPane =
                createControlScrollPane();

        add(
                header,
                BorderLayout.NORTH
        );

        add(
                center,
                BorderLayout.CENTER
        );

        add(
                controlScrollPane,
                BorderLayout.EAST
        );

        ((JComponent) getContentPane())
                .setBorder(
                        new EmptyBorder(
                                16,
                                16,
                                16,
                                16
                        )
                );

        setSize(
                1250,
                820
        );

        setMinimumSize(
                new Dimension(
                        1000,
                        650
                )
        );

        setLocationRelativeTo(null);
    }

    /*
     * =========================================================
     * CABEÇALHO
     * =========================================================
     */

    private JPanel createHeader() {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setOpaque(false);

        JLabel title =
                new JLabel(
                        "ROTAS"
                );

        title.setForeground(TEXT);

        title.setFont(
                title.getFont()
                        .deriveFont(
                                Font.BOLD,
                                24f
                        )
        );

        JLabel subtitle =
                new JLabel(
                        "Visualização de A* e Greedy Best-First Search"
                );

        subtitle.setForeground(MUTED);

        panel.add(
                title,
                BorderLayout.WEST
        );

        panel.add(
                subtitle,
                BorderLayout.EAST
        );

        return panel;
    }

    private JScrollPane createControlScrollPane() {

        JPanel controlPanel =
                createControlPanel();

        JScrollPane scrollPane =
                new JScrollPane(controlPanel);

        /*
         * Largura total do painel lateral.
         *
         * Incluímos espaço suficiente para a scrollbar.
         */
        scrollPane.setPreferredSize(
                new Dimension(
                        360,
                        700
                )
        );

        scrollPane.setMinimumSize(
                new Dimension(
                        340,
                        400
                )
        );

        scrollPane.setBorder(null);

        scrollPane.setBackground(PANEL);

        scrollPane
                .getViewport()
                .setBackground(PANEL);

        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        /*
         * Não deve existir rolagem horizontal.
         */
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane
                .getVerticalScrollBar()
                .setUnitIncrement(16);

        return scrollPane;
    }

    /*
     * =========================================================
     * CONTAINER DO GRID
     * =========================================================
     */

    private JPanel createGridContainer() {

        JPanel center =
                new JPanel(
                        new BorderLayout()
                );

        center.setBackground(PANEL);

        center.setBorder(
                BorderFactory
                        .createCompoundBorder(

                                new LineBorder(
                                        BORDER,
                                        1,
                                        true
                                ),

                                new EmptyBorder(
                                        12,
                                        12,
                                        12,
                                        12
                                )
                        )
        );

        center.add(
                gridPanel,
                BorderLayout.CENTER
        );

        center.add(
                createLegend(),
                BorderLayout.SOUTH
        );

        return center;
    }

    /*
     * =========================================================
     * SCROLL DO PAINEL LATERAL
     * =========================================================
     */

    /*
     * =========================================================
     * PAINEL DE CONTROLES
     * =========================================================
     */

    private JPanel createControlPanel() {

        ScrollablePanel container =
                new ScrollablePanel();

        container.setBackground(PANEL);

        container.setBorder(
                new EmptyBorder(
                        18,
                        18,
                        18,
                        18
                )
        );

        container.setLayout(
                new BoxLayout(
                        container,
                        BoxLayout.Y_AXIS
                )
        );

        /*
         * =====================================================
         * EXECUÇÃO
         * =====================================================
         */

        addSectionTitle(
                container,
                "EXECUÇÃO"
        );

        algorithmCombo =
                new JComboBox<>(
                        new String[]{
                                "A*",
                                "Greedy Best-First"
                        }
                );

        styleComboBox(
                algorithmCombo
        );

        container.add(
                algorithmCombo
        );

        container.add(
                verticalSpace(10)
        );

        JButton runButton =
                createPrimaryButton(
                        "▶  Executar"
                );

        runButton.addActionListener(
                e -> executeSearch()
        );

        container.add(
                runButton
        );

        container.add(
                verticalSpace(14)
        );

        addSmallLabel(
                container,
                "Velocidade da animação"
        );

        speedSlider =
                new JSlider(
                        20,
                        500,
                        120
                );

        configureSlider(
                speedSlider
        );

        /*
         * Tooltip explicativo.
         *
         * Valores menores = animação mais rápida,
         * pois representam o atraso do Timer.
         */
        speedSlider.setToolTipText(
                "Menor valor = animação mais rápida"
        );

        container.add(
                speedSlider
        );

        container.add(
                verticalSpace(22)
        );

        /*
         * =====================================================
         * EDITOR
         * =====================================================
         */

        addSectionTitle(
                container,
                "EDITOR"
        );

        JPanel editorGrid =
                new JPanel(
                        new GridLayout(
                                2,
                                2,
                                8,
                                8
                        )
                );

        editorGrid.setOpaque(false);

        editorGrid.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        86
                )
        );

        wallButton =
                createSecondaryButton(
                        "Parede"
                );

        eraseButton =
                createSecondaryButton(
                        "Borracha"
                );

        startButton =
                createSecondaryButton(
                        "Origem A"
                );

        goalButton =
                createSecondaryButton(
                        "Destino B"
                );

        wallButton.setToolTipText(
                "Clique e arraste para desenhar paredes"
        );

        eraseButton.setToolTipText(
                "Clique e arraste para apagar paredes"
        );

        startButton.setToolTipText(
                "Selecione e clique no mapa para posicionar A"
        );

        goalButton.setToolTipText(
                "Selecione e clique no mapa para posicionar B"
        );

        wallButton.addActionListener(
                e -> selectEditMode(
                        GridPanel
                                .EditMode
                                .WALL
                )
        );

        eraseButton.addActionListener(
                e -> selectEditMode(
                        GridPanel
                                .EditMode
                                .ERASE
                )
        );

        startButton.addActionListener(
                e -> selectEditMode(
                        GridPanel
                                .EditMode
                                .START
                )
        );

        goalButton.addActionListener(
                e -> selectEditMode(
                        GridPanel
                                .EditMode
                                .GOAL
                )
        );

        editorGrid.add(wallButton);
        editorGrid.add(eraseButton);
        editorGrid.add(startButton);
        editorGrid.add(goalButton);

        container.add(
                editorGrid
        );

        /*
         * Ferramenta inicial.
         */
        selectEditMode(
                GridPanel.EditMode.WALL
        );

        container.add(
                verticalSpace(22)
        );

        /*
         * =====================================================
         * MAPA
         * =====================================================
         */

        addSectionTitle(
                container,
                "MAPA"
        );

        JButton defaultButton =
                createSecondaryButton(
                        "Restaurar mapa padrão"
                );

        defaultButton.addActionListener(
                e -> {

                    stopAnimation();

                    grid.createDefaultMap();

                    gridPanel
                            .clearSearchVisualization();

                    resetMetrics();
                }
        );

        container.add(
                defaultButton
        );

        container.add(
                verticalSpace(8)
        );

        JButton clearButton =
                createSecondaryButton(
                        "Limpar mapa"
                );

        clearButton.addActionListener(
                e -> {

                    stopAnimation();

                    grid.clear();

                    gridPanel
                            .clearSearchVisualization();

                    resetMetrics();
                }
        );

        container.add(
                clearButton
        );

        container.add(
                verticalSpace(14)
        );

        JButton randomButton =
                createSecondaryButton(
                        "Gerar obstáculos"
                );

        randomButton.addActionListener(
                e -> generateRandomMap()
        );

        container.add(
                randomButton
        );

        container.add(
                verticalSpace(22)
        );

        /*
         * =====================================================
         * RESULTADO
         * =====================================================
         */

        addSectionTitle(
                container,
                "RESULTADO ATUAL"
        );

        JPanel currentResult =
                createCurrentResultCard();

        container.add(
                currentResult
        );

        container.add(
                verticalSpace(18)
        );

        /*
         * =====================================================
         * COMPARAÇÃO
         * =====================================================
         */

        JButton compareButton =
                createPrimaryButton(
                        "Comparar A* × Greedy"
                );

        compareButton.addActionListener(
                e -> compareAlgorithms()
        );

        container.add(
                compareButton
        );

        container.add(
                verticalSpace(12)
        );

        JPanel comparison =
                createComparisonCard();

        container.add(
                comparison
        );

        /*
         * Pequeno espaço no final para evitar
         * que o último card fique colado na borda.
         */
        container.add(
                verticalSpace(10)
        );

        return container;
    }

    /*
     * =========================================================
     * RESULTADO ATUAL
     * =========================================================
     */

    private JPanel createCurrentResultCard() {

        JPanel panel =
                createCardPanel();

        panel.setLayout(
                new GridLayout(
                        4,
                        1,
                        0,
                        6
                )
        );

        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        120
                )
        );

        statusLabel =
                createMetricLabel(
                        "Pronto"
                );

        nodesLabel =
                createMetricLabel(
                        "Nós explorados: —"
                );

        costLabel =
                createMetricLabel(
                        "Custo: —"
                );

        timeLabel =
                createMetricLabel(
                        "Tempo: —"
                );

        panel.add(statusLabel);
        panel.add(nodesLabel);
        panel.add(costLabel);
        panel.add(timeLabel);

        return panel;
    }

    /*
     * =========================================================
     * CARD DE COMPARAÇÃO
     * =========================================================
     */

    private JPanel createComparisonCard() {

        JPanel panel =
                createCardPanel();

        panel.setLayout(
                new GridLayout(
                        4,
                        3,
                        8,
                        7
                )
        );

        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        130
                )
        );

        /*
         * Cabeçalho.
         */
        panel.add(
                createMutedLabel("")
        );

        panel.add(
                createBoldLabel("A*")
        );

        panel.add(
                createBoldLabel("Greedy")
        );

        /*
         * Nós.
         */
        panel.add(
                createMutedLabel("Nós")
        );

        aStarNodesLabel =
                createMetricLabel("—");

        greedyNodesLabel =
                createMetricLabel("—");

        panel.add(
                aStarNodesLabel
        );

        panel.add(
                greedyNodesLabel
        );

        /*
         * Custo.
         */
        panel.add(
                createMutedLabel("Custo")
        );

        aStarCostLabel =
                createMetricLabel("—");

        greedyCostLabel =
                createMetricLabel("—");

        panel.add(
                aStarCostLabel
        );

        panel.add(
                greedyCostLabel
        );

        /*
         * Tempo.
         */
        panel.add(
                createMutedLabel("Tempo")
        );

        aStarTimeLabel =
                createMetricLabel("—");

        greedyTimeLabel =
                createMetricLabel("—");

        panel.add(
                aStarTimeLabel
        );

        panel.add(
                greedyTimeLabel
        );

        return panel;
    }

    /*
     * =========================================================
     * LEGENDA
     * =========================================================
     */

    private JPanel createLegend() {

        JPanel panel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                18,
                                8
                        )
                );

        panel.setOpaque(false);

        panel.add(
                createLegendItem(
                        new Color(
                                250,
                                204,
                                21
                        ),
                        "OPEN"
                )
        );

        panel.add(
                createLegendItem(
                        new Color(
                                249,
                                115,
                                22
                        ),
                        "CLOSED"
                )
        );

        panel.add(
                createLegendItem(
                        new Color(
                                168,
                                85,
                                247
                        ),
                        "Atual"
                )
        );

        panel.add(
                createLegendItem(
                        new Color(
                                59,
                                130,
                                246
                        ),
                        "Caminho"
                )
        );

        panel.add(
                createLegendItem(
                        new Color(
                                16,
                                185,
                                129
                        ),
                        "Origem"
                )
        );

        panel.add(
                createLegendItem(
                        new Color(
                                239,
                                68,
                                68
                        ),
                        "Destino"
                )
        );

        return panel;
    }

    private JPanel createLegendItem(
            Color color,
            String text
    ) {

        JPanel item =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                5,
                                0
                        )
                );

        item.setOpaque(false);

        JLabel square =
                new JLabel("■");

        square.setForeground(color);

        JLabel label =
                new JLabel(text);

        label.setForeground(MUTED);

        item.add(square);
        item.add(label);

        return item;
    }

    /*
     * =========================================================
     * EDITOR
     * =========================================================
     */

    private void selectEditMode(
            GridPanel.EditMode mode
    ) {

        gridPanel.setEditMode(mode);

        /*
         * Esse método pode ser chamado durante
         * a criação dos próprios botões.
         */
        if (wallButton == null
                || eraseButton == null
                || startButton == null
                || goalButton == null) {

            return;
        }

        JButton[] buttons = {
                wallButton,
                eraseButton,
                startButton,
                goalButton
        };

        /*
         * Estado normal.
         */
        for (JButton button :
                buttons) {

            button.setBackground(CARD);
            button.setForeground(TEXT);
        }

        /*
         * Descobrimos qual botão representa
         * a ferramenta atual.
         */
        JButton selected =
                switch (mode) {

                    case WALL ->
                            wallButton;

                    case ERASE ->
                            eraseButton;

                    case START ->
                            startButton;

                    case GOAL ->
                            goalButton;
                };

        /*
         * Estado selecionado.
         */
        selected.setBackground(
                ACCENT
        );

        selected.setForeground(
                Color.WHITE
        );

        repaint();
    }

    /*
     * =========================================================
     * MAPA ALEATÓRIO
     * =========================================================
     */

    private void generateRandomMap() {

        stopAnimation();

        /*
         * Caso A não exista, criamos uma origem padrão.
         */
        if (grid.getStart() == null) {

            grid.setStart(
                    new Position(
                            2,
                            2
                    )
            );
        }

        /*
         * Caso B não exista, criamos um destino padrão.
         */
        if (grid.getGoal() == null) {

            grid.setGoal(
                    new Position(
                            grid.getRows() - 3,
                            grid.getColumns() - 3
                    )
            );
        }

        grid.generateRandomWalls(0.25);

        gridPanel
                .clearSearchVisualization();

        resetMetrics();
    }

    /*
     * =========================================================
     * EXECUÇÃO DO ALGORITMO
     * =========================================================
     */

    private void executeSearch() {

        stopAnimation();

        if (!validateMap()) {
            return;
        }

        gridPanel
                .clearSearchVisualization();

        gridPanel
                .setEditingEnabled(false);

        SearchAlgorithm algorithm;

        if (algorithmCombo
                .getSelectedIndex() == 0) {

            algorithm =
                    new AStarSearch();

        } else {

            algorithm =
                    new GreedySearch();
        }

        SearchResult result =
                algorithm.search(grid);

        /*
         * Guardamos o resultado para
         * o painel comparativo.
         */
        if (algorithm
                instanceof AStarSearch) {

            lastAStarResult =
                    result;

        } else {

            lastGreedyResult =
                    result;
        }

        animateResult(
                result
        );
    }

    /*
     * =========================================================
     * ANIMAÇÃO
     * =========================================================
     */

    private void animateResult(
            SearchResult result
    ) {

        if (result
                .getSteps()
                .isEmpty()) {

            finishAnimation(
                    result
            );

            return;
        }

        final int[] index = {0};

        animationTimer =
                new Timer(
                        speedSlider
                                .getValue(),
                        null
                );

        animationTimer
                .addActionListener(
                        e -> {

                            if (index[0]
                                    < result
                                    .getSteps()
                                    .size()) {

                                SearchStep step =
                                        result
                                                .getSteps()
                                                .get(
                                                        index[0]
                                                );

                                gridPanel
                                        .showStep(
                                                step
                                        );

                                statusLabel
                                        .setText(
                                                "Explorando passo "
                                                        + (
                                                        index[0]
                                                                + 1
                                                )
                                                        + " / "
                                                        + result
                                                        .getSteps()
                                                        .size()
                                        );

                                nodesLabel
                                        .setText(
                                                "Nós explorados: "
                                                        + step
                                                        .getClosed()
                                                        .size()
                                        );

                                index[0]++;

                            } else {

                                stopAnimation();

                                gridPanel
                                        .showPath(
                                                result
                                                        .getPath()
                                        );

                                finishAnimation(
                                        result
                                );
                            }
                        }
                );

        animationTimer.start();
    }

    /*
     * =========================================================
     * FINALIZAÇÃO
     * =========================================================
     */

    private void finishAnimation(
            SearchResult result
    ) {

        gridPanel
                .setEditingEnabled(true);

        if (result.isFound()) {

            statusLabel.setText(
                    "Caminho encontrado · "
                            + result
                            .getAlgorithmName()
            );

        } else {

            statusLabel.setText(
                    "Nenhum caminho encontrado"
            );
        }

        nodesLabel.setText(
                "Nós explorados: "
                        + result
                        .getExploredNodes()
        );

        if (result.isFound()) {

            costLabel.setText(
                    String.format(
                            "Custo: %.0f",
                            result
                                    .getPathCost()
                    )
            );

        } else {

            costLabel.setText(
                    "Custo: —"
            );
        }

        timeLabel.setText(
                String.format(
                        "Tempo: %.3f ms",
                        result
                                .getExecutionTimeMs()
                )
        );

        updateComparisonCard();
    }

    /*
     * =========================================================
     * COMPARAÇÃO
     * =========================================================
     */

    private void compareAlgorithms() {

        stopAnimation();

        if (!validateMap()) {
            return;
        }

        gridPanel
                .clearSearchVisualization();

        /*
         * Ambos são executados exatamente
         * sobre o mesmo mapa.
         */
        lastAStarResult =
                new AStarSearch()
                        .search(grid);

        lastGreedyResult =
                new GreedySearch()
                        .search(grid);

        updateComparisonCard();

        statusLabel.setText(
                "Comparação atualizada"
        );

        nodesLabel.setText(
                "Nós explorados: —"
        );

        costLabel.setText(
                "Custo: —"
        );

        timeLabel.setText(
                "Tempo: —"
        );
    }

    private void updateComparisonCard() {

        /*
         * A*
         */
        if (lastAStarResult != null) {

            aStarNodesLabel
                    .setText(
                            String.valueOf(
                                    lastAStarResult
                                            .getExploredNodes()
                            )
                    );

            if (lastAStarResult
                    .isFound()) {

                aStarCostLabel
                        .setText(
                                String.format(
                                        "%.0f",
                                        lastAStarResult
                                                .getPathCost()
                                )
                        );

            } else {

                aStarCostLabel
                        .setText("—");
            }

            aStarTimeLabel
                    .setText(
                            String.format(
                                    "%.3f ms",
                                    lastAStarResult
                                            .getExecutionTimeMs()
                            )
                    );
        }

        /*
         * Greedy.
         */
        if (lastGreedyResult != null) {

            greedyNodesLabel
                    .setText(
                            String.valueOf(
                                    lastGreedyResult
                                            .getExploredNodes()
                            )
                    );

            if (lastGreedyResult
                    .isFound()) {

                greedyCostLabel
                        .setText(
                                String.format(
                                        "%.0f",
                                        lastGreedyResult
                                                .getPathCost()
                                )
                        );

            } else {

                greedyCostLabel
                        .setText("—");
            }

            greedyTimeLabel
                    .setText(
                            String.format(
                                    "%.3f ms",
                                    lastGreedyResult
                                            .getExecutionTimeMs()
                            )
                    );
        }
    }

    /*
     * =========================================================
     * VALIDAÇÃO
     * =========================================================
     */

    private boolean validateMap() {

        if (grid.getStart() != null
                && grid.getGoal() != null) {

            return true;
        }

        JOptionPane.showMessageDialog(
                this,
                "Defina a origem A e o destino B antes de executar.",
                "Mapa incompleto",
                JOptionPane.WARNING_MESSAGE
        );

        return false;
    }

    /*
     * =========================================================
     * PARAR ANIMAÇÃO
     * =========================================================
     */

    private void stopAnimation() {

        if (animationTimer != null) {

            animationTimer.stop();

            animationTimer = null;
        }

        gridPanel
                .setEditingEnabled(true);
    }

    /*
     * =========================================================
     * RESET DE MÉTRICAS
     * =========================================================
     */

    private void resetMetrics() {

        statusLabel.setText(
                "Pronto"
        );

        nodesLabel.setText(
                "Nós explorados: —"
        );

        costLabel.setText(
                "Custo: —"
        );

        timeLabel.setText(
                "Tempo: —"
        );

        lastAStarResult = null;
        lastGreedyResult = null;

        aStarNodesLabel
                .setText("—");

        aStarCostLabel
                .setText("—");

        aStarTimeLabel
                .setText("—");

        greedyNodesLabel
                .setText("—");

        greedyCostLabel
                .setText("—");

        greedyTimeLabel
                .setText("—");
    }

    /*
     * =========================================================
     * COMPONENTES VISUAIS
     * =========================================================
     */

    private JButton createPrimaryButton(
            String text
    ) {

        JButton button =
                new JButton(text);

        button.setBackground(
                ACCENT
        );

        button.setForeground(
                Color.WHITE
        );

        button.setFocusPainted(
                false
        );

        button.setOpaque(
                true
        );

        button.setContentAreaFilled(
                true
        );

        button.setBorderPainted(
                false
        );

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        button.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        42
                )
        );

        button.setPreferredSize(
                new Dimension(
                        250,
                        42
                )
        );

        return button;
    }

    private JButton createSecondaryButton(
            String text
    ) {

        JButton button =
                new JButton(text);

        button.setBackground(
                CARD
        );

        button.setForeground(
                TEXT
        );

        button.setFocusPainted(
                false
        );

        /*
         * Estas três configurações são importantes
         * principalmente no Linux.
         */
        button.setOpaque(
                true
        );

        button.setContentAreaFilled(
                true
        );

        button.setBorderPainted(
                true
        );

        button.setBorder(
                new LineBorder(
                        BORDER,
                        1,
                        true
                )
        );

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        button.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        40
                )
        );

        button.setPreferredSize(
                new Dimension(
                        120,
                        40
                )
        );

        return button;
    }

    private void styleComboBox(
            JComboBox<String> comboBox
    ) {

        comboBox.setBackground(
                CARD
        );

        comboBox.setForeground(
                TEXT
        );

        comboBox.setOpaque(
                true
        );

        comboBox.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        40
                )
        );

        comboBox.setPreferredSize(
                new Dimension(
                        250,
                        40
                )
        );

        comboBox.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );
    }

    private void configureSlider(
            JSlider slider
    ) {

        slider.setOpaque(false);

        slider.setForeground(
                ACCENT
        );

        slider.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        slider.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        40
                )
        );
    }

    private JPanel createCardPanel() {

        JPanel panel =
                new JPanel();

        panel.setBackground(
                CARD
        );

        panel.setBorder(
                BorderFactory
                        .createCompoundBorder(

                                new LineBorder(
                                        BORDER,
                                        1,
                                        true
                                ),

                                new EmptyBorder(
                                        12,
                                        12,
                                        12,
                                        12
                                )
                        )
        );

        panel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        return panel;
    }

    private void addSectionTitle(
            JPanel panel,
            String text
    ) {

        JLabel label =
                new JLabel(text);

        label.setForeground(
                TEXT
        );

        label.setFont(
                label.getFont()
                        .deriveFont(
                                Font.BOLD,
                                12f
                        )
        );

        label.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        panel.add(label);

        panel.add(
                verticalSpace(8)
        );
    }

    private void addSmallLabel(
            JPanel panel,
            String text
    ) {

        JLabel label =
                createMutedLabel(
                        text
                );

        label.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        panel.add(label);

        panel.add(
                verticalSpace(4)
        );
    }

    private JLabel createMetricLabel(
            String text
    ) {

        JLabel label =
                new JLabel(text);

        label.setForeground(
                TEXT
        );

        return label;
    }

    private JLabel createMutedLabel(
            String text
    ) {

        JLabel label =
                new JLabel(text);

        label.setForeground(
                MUTED
        );

        return label;
    }

    private JLabel createBoldLabel(
            String text
    ) {

        JLabel label =
                new JLabel(text);

        label.setForeground(
                TEXT
        );

        label.setFont(
                label.getFont()
                        .deriveFont(
                                Font.BOLD
                        )
        );

        return label;
    }

    private Component verticalSpace(
            int height
    ) {

        return Box.createRigidArea(
                new Dimension(
                        0,
                        height
                )
        );
    }
}