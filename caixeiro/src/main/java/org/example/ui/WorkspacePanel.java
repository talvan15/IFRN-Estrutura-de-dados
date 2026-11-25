package org.example.ui;

import org.example.algorithm.AlgorithmStep;
import org.example.algorithm.BruteForceTsp;
import org.example.algorithm.DijkstraTsp;
import org.example.algorithm.RouteEvaluation;
import org.example.algorithm.TspResult;
import org.example.model.City;
import org.example.model.Edge;
import org.example.model.Graph;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/** Main window for building a graph and comparing the PCV algorithms. */
@SuppressWarnings("serial")
final class WorkspacePanel extends JPanel {
    private static final Color PAGE = new Color(237, 242, 245);
    private static final Color INK = new Color(24, 42, 56);
    private static final Color MUTED = new Color(73, 94, 109);
    private static final Color LINE = new Color(216, 225, 231);
    private static final Color ACCENT = new Color(17, 105, 87);

    private final GraphPanel graphPanel;
    private final DefaultListModel<City> cities = new DefaultListModel<>();
    private final JList<City> cityList = new JList<>(cities);
    private final JComboBox<City> originSelector = new JComboBox<>();
    private final JButton runButton = new ActionButton("Calcular rota", true);
    private final JButton renameButton = new ActionButton("Renomear", false);
    private final JButton addCityButton = new ActionButton("Adicionar", false);
    private final JButton roadButton = new ActionButton("Adicionar estrada", false);
    private final JButton presetButton = new ActionButton("Carregar exemplo", false);
    private final JButton newGraphButton = new ActionButton("Novo grafo", false);
    private final JLabel graphSummary = label("", 13, false, MUTED);
    private final JLabel status = label("Escolha um algoritmo e calcule a rota.", 13, false, MUTED);
    private final JTabbedPane tabs = new JTabbedPane();
    private final EnumMap<Algorithm, ResultView> views = new EnumMap<>(Algorithm.class);
    private Graph graph;
    private boolean refreshing;
    private boolean running;

    WorkspacePanel(Graph initialGraph) {
        super(new BorderLayout(0, 16));
        graph = initialGraph;
        graphPanel = new GraphPanel(graph);
        setBackground(PAGE);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        JPanel title = stack(2);
        title.add(label("Caixeiro Viajante", 30, true, INK));
        title.add(label("Monte um grafo e compare as rotas de menor custo.", 14, false, MUTED));
        header.add(title, BorderLayout.WEST);
        header.add(actions(presetButton, newGraphButton), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel map = card("Mapa de cidades");
        map.add(graphPanel, BorderLayout.CENTER);
        JPanel mapFooter = new JPanel(new BorderLayout(8, 0));
        mapFooter.setOpaque(false);
        mapFooter.add(graphSummary, BorderLayout.WEST);
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 0));
        legend.setOpaque(false);
        legend.add(label("● Origem", 12, false, GraphPanel.ORIGIN_COLOR));
        legend.add(label("━ Melhor rota", 12, false, GraphPanel.ROUTE_COLOR));
        mapFooter.add(legend, BorderLayout.EAST);
        map.add(mapFooter, BorderLayout.SOUTH);

        JPanel results = card("Comparação");
        JPanel comparison = new JPanel(new GridLayout(1, 0, 8, 0));
        comparison.setOpaque(false);
        tabs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        for (Algorithm algorithm : Algorithm.values()) {
            ResultView view = new ResultView(algorithm);
            comparison.add(view.comparison);
            views.put(algorithm, view);
            tabs.addTab(algorithm.label, view);
        }
        JPanel resultContent = new JPanel(new BorderLayout(0, 10));
        resultContent.setOpaque(false);
        resultContent.add(comparison, BorderLayout.NORTH);
        resultContent.add(tabs, BorderLayout.CENTER);
        results.add(resultContent, BorderLayout.CENTER);
        JLabel originLabel = label("Origem", 13, true, INK);
        originLabel.setLabelFor(originSelector);
        originSelector.setRenderer(cityRenderer());
        originSelector.setPrototypeDisplayValue(new City("Cidade de origem"));
        JPanel controls = new JPanel(new BorderLayout(10, 0));
        controls.setOpaque(false);
        controls.add(originLabel, BorderLayout.WEST);
        controls.add(originSelector, BorderLayout.CENTER);
        controls.add(runButton, BorderLayout.EAST);
        results.add(controls, BorderLayout.SOUTH);

        JPanel editor = card("Cidades");
        cityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cityList.setCellRenderer(cityRenderer());
        cityList.setFixedCellHeight(32);
        cityList.getAccessibleContext().setAccessibleName("Cidades do grafo");
        JScrollPane cityScroll = scroll(cityList);
        cityScroll.setPreferredSize(new Dimension(174, 200));
        editor.add(cityScroll, BorderLayout.CENTER);

        JPanel editorActions = stack(6);
        editorActions.add(addCityButton);
        editorActions.add(renameButton);
        editorActions.add(roadButton);
        editor.add(editorActions, BorderLayout.SOUTH);

        JPanel workspace = new JPanel(new BorderLayout(16, 0));
        workspace.setOpaque(false);
        workspace.add(editor, BorderLayout.WEST);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, map, results);
        split.setBorder(null);
        split.setOpaque(false);
        split.setContinuousLayout(true);
        split.setResizeWeight(0.54);
        split.setDividerSize(10);
        map.setMinimumSize(new Dimension(300, 260));
        results.setMinimumSize(new Dimension(350, 340));
        map.setPreferredSize(new Dimension(500, 470));
        results.setPreferredSize(new Dimension(375, 470));
        workspace.add(split, BorderLayout.CENTER);
        add(workspace, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        refreshGraph(null);
        cityList.addListSelectionListener(event -> updateControls());
        originSelector.addActionListener(event -> {
            if (!refreshing) {
                clearResults();
                status.setText("Origem alterada.");
            }
        });
        tabs.addChangeListener(event -> showSelectedResult());
        runButton.addActionListener(event -> runSelected());
        renameButton.addActionListener(event -> editCity(cityList.getSelectedValue()));
        addCityButton.addActionListener(event -> editCity(null));
        roadButton.addActionListener(event -> addRoad());
        presetButton.addActionListener(event -> loadPreset());
        newGraphButton.addActionListener(event -> createGraph());

    }

    private void loadPreset() {
        if (confirm("Carregar o exemplo Circuito (5 cidades) e substituir o grafo atual?")) {
            replaceGraph(circuitPreset(), null, "Exemplo Circuito carregado.");
        }
    }

    static Graph circuitPreset() {
        Graph example = new Graph();
        List<City> places = List.of(new City("Centro"), new City("Museu"), new City("Parque"),
                new City("Mercado"), new City("Estação"));
        places.forEach(example::addCity);
        for (int i = 0; i < places.size(); i++) {
            example.addEdge(places.get(i), places.get((i + 1) % places.size()), i + 2);
        }
        example.addEdge(places.get(0), places.get(2), 7);
        example.addEdge(places.get(1), places.get(3), 8);
        return example;
    }

    private void createGraph() {
        String input = JOptionPane.showInputDialog(this,
                "Informe quantas cidades terá o novo grafo. Cada par receberá uma estrada com peso de 1 a 20.", "5");
        if (input == null) return;

        final int count;
        try {
            count = Integer.parseInt(input.strip());
            if (count < 1) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            showError("Informe um número inteiro positivo de cidades.");
            return;
        }

        long roads = (long) count * (count - 1) / 2;
        if (roads > 10_000 && !confirm("O novo grafo terá " + roads
                + " estradas e pode consumir bastante memória. Continuar?")) return;

        replaceGraph(generateCompleteGraph(count), null,
                count == 1 ? "Grafo criado com uma cidade." : "Grafo criado com estradas e pesos automáticos.");
    }

    private static Graph generateCompleteGraph(int count) {
        Graph generated = new Graph();
        for (int i = 0; i < count; i++) generated.addCity(new City("V" + (i + 1)));

        List<City> places = generated.cities();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < places.size(); i++) {
            for (int j = i + 1; j < places.size(); j++) {
                generated.addEdge(places.get(i), places.get(j), random.nextInt(1, 21));
            }
        }
        return generated;
    }

    private void editCity(City previous) {
        String title = previous == null ? "Adicionar cidade" : "Renomear cidade";
        JTextField nameField = new JTextField(previous == null ? "" : previous.name(), 22);
        JLabel nameLabel = new JLabel("Nome da cidade");
        nameLabel.setLabelFor(nameField);
        JPanel form = stack(6);
        form.add(nameLabel);
        form.add(nameField);
        while (JOptionPane.showConfirmDialog(this, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            String name = nameField.getText().strip();
            if (name.isEmpty()) {
                showError("Digite um nome para a cidade.");
                continue;
            }
            City updated = new City(name);
            if (updated.equals(previous)) return;
            if (graph.containsCity(updated)) {
                showError("Já existe uma cidade com esse nome.");
                continue;
            }

            applyCityEdit(previous, updated);
            return;
        }
    }

    private void applyCityEdit(City previous, City updated) {
        City origin = (City) originSelector.getSelectedItem();
        if (previous == null) {
            graph.addCity(updated);
        } else {
            Graph renamed = new Graph();
            for (City city : graph.cities()) renamed.addCity(city.equals(previous) ? updated : city);
            for (Edge edge : graph.edges()) {
                renamed.addEdge(edge.first().equals(previous) ? updated : edge.first(),
                        edge.second().equals(previous) ? updated : edge.second(), edge.weight());
            }
            graph = renamed;
        }

        replaceGraph(graph, previous != null && previous.equals(origin) ? updated : origin,
                previous == null ? "Cidade adicionada." : "Cidade renomeada.");
        cityList.setSelectedValue(updated, true);
    }

    private void addRoad() {
        City[] options = graph.cities().toArray(City[]::new);
        JComboBox<City> from = new JComboBox<>(options);
        JComboBox<City> to = new JComboBox<>(options);
        from.setRenderer(cityRenderer());
        to.setRenderer(cityRenderer());
        from.setPrototypeDisplayValue(new City("Selecione uma cidade"));
        to.setPrototypeDisplayValue(new City("Selecione uma cidade"));
        to.setSelectedIndex(1);
        JSpinner weight = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        addField(form, "De", from);
        addField(form, "Para", to);
        addField(form, "Peso", weight);
        while (JOptionPane.showConfirmDialog(this, form, "Adicionar estrada",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            City first = (City) from.getSelectedItem();
            City second = (City) to.getSelectedItem();
            try {
                weight.commitEdit();
                if (first.equals(second)) {
                    showError("Selecione duas cidades diferentes.");
                } else if (graph.weightBetween(first, second).isPresent()) {
                    showError("Já existe uma estrada entre essas cidades.");
                } else {
                    graph.addEdge(first, second, (int) weight.getValue());
                    refreshGraph((City) originSelector.getSelectedItem());
                    status.setText("Estrada adicionada.");
                    return;
                }
            } catch (java.text.ParseException exception) {
                showError("Informe um peso inteiro igual ou maior que zero.");
            }
        }
    }

    private void replaceGraph(Graph replacement, City origin, String message) {
        graph = replacement;
        refreshGraph(origin);
        status.setText(message);
    }

    private void refreshGraph(City origin) {
        refreshing = true;
        List<City> available = graph.cities();
        cities.clear();
        cities.addAll(available);
        if (!available.isEmpty()) cityList.setSelectedIndex(0);
        originSelector.setModel(new DefaultComboBoxModel<>(available.toArray(City[]::new)));
        if (origin != null) originSelector.setSelectedItem(origin);
        graphPanel.setGraph(graph);
        int roads = graph.edges().size();
        graphSummary.setText(available.size() + (available.size() == 1 ? " cidade" : " cidades")
                + " · " + roads + (roads == 1 ? " estrada" : " estradas"));
        refreshing = false;
        clearResults();
        updateControls();
    }

    private void clearResults() {
        views.values().forEach(ResultView::clear);
        showSelectedResult();
    }

    private void showSelectedResult() {
        graphPanel.showOrigin((City) originSelector.getSelectedItem());
        ResultView view = views.get(selectedAlgorithm());
        graphPanel.showRoute(view.bestRoute);
    }

    private void updateControls() {
        runButton.setEnabled(!running && originSelector.getItemCount() > 0);
        originSelector.setEnabled(!running);
        renameButton.setEnabled(!running && !cityList.isSelectionEmpty());
        roadButton.setEnabled(!running && cities.size() > 1);
        addCityButton.setEnabled(!running);
        presetButton.setEnabled(!running);
        newGraphButton.setEnabled(!running);
    }

    private void runSelected() {
        Algorithm algorithm = selectedAlgorithm();
        City origin = (City) originSelector.getSelectedItem();
        String warning = performanceWarning(algorithm, cities.size());
        if (warning != null && !confirm(warning)) return;

        Graph input = graph;
        ResultView view = views.get(algorithm);
        running = true;
        updateControls();
        view.showMessage("Em cálculo", "Aguarde a conclusão da busca.");
        status.setText("Executando " + algorithm.label + "…");
        showSelectedResult();
        new SwingWorker<RunOutput, Void>() {
            @Override
            protected RunOutput doInBackground() {
                long start = System.nanoTime();
                TspResult result = switch (algorithm) {
                    case BRUTE_FORCE -> new BruteForceTsp().solve(input, origin);
                    case DIJKSTRA -> new DijkstraTsp().solve(input, origin);
                };
                long elapsedNanos = System.nanoTime() - start;
                return new RunOutput(result, elapsedNanos, formatDetails(algorithm, result));
            }

            @Override
            protected void done() {
                try {
                    view.showResult(get());
                    status.setText(algorithm.label + " · cálculo concluído.");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    view.showMessage("Interrompido", "Clique em Calcular rota para tentar novamente.");
                    status.setText("Cálculo interrompido.");
                } catch (ExecutionException exception) {
                    view.showMessage("Falha no cálculo", "Tente novamente com um grafo menor.");
                    status.setText("Falha no cálculo.");
                } finally {
                    running = false;
                    updateControls();
                    showSelectedResult();
                }
            }
        }.execute();
    }

    private Algorithm selectedAlgorithm() {
        return Algorithm.values()[tabs.getSelectedIndex()];
    }

    private static String performanceWarning(Algorithm algorithm, int count) {
        if (algorithm == Algorithm.BRUTE_FORCE && count >= 10) {
            BigInteger permutations = BigInteger.ONE;
            for (int i = 2; i < count; i++) permutations = permutations.multiply(BigInteger.valueOf(i));
            return "A força bruta vai verificar " + permutations + " rotas. O cálculo pode demorar e consumir muita memória. Continuar?";
        }
        if (algorithm == Algorithm.DIJKSTRA && count >= 17) {
            return "O número de estados do Dijkstra cresce rapidamente com o tamanho do grafo. Continuar?";
        }
        return null;
    }

    private static String formatDetails(Algorithm algorithm, TspResult result) {
        StringBuilder details = new StringBuilder();
        if (algorithm == Algorithm.BRUTE_FORCE) {
            details.append(result.evaluatedRoutes().size()).append(" rotas avaliadas\n\n");
            for (RouteEvaluation route : result.evaluatedRoutes()) {
                details.append(formatRoute(route.route())).append("   ·   ");
                details.append(route.cost().isPresent() ? route.cost().getAsLong() : "sem conexão");
                if (route.cost().isPresent() && route.cost().getAsLong() == result.bestCost().orElse(-1)) {
                    details.append("   ·   menor custo");
                }
                details.append('\n');
            }
        } else {
            details.append(result.steps().size()).append(" passos da busca\n\n");
            for (AlgorithmStep step : result.steps()) {
                details.append(step.description()).append('\n');
                details.append(formatRoute(step.path()));
                step.cost().ifPresent(cost -> details.append("   ·   custo ").append(cost));
                details.append("\n\n");
            }
        }
        return details.toString();
    }

    private static String formatRoute(List<City> route) {
        return route.stream().map(City::name).collect(Collectors.joining(" → "));
    }

    private boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Confira os dados", JOptionPane.ERROR_MESSAGE);
    }

    private static void addField(JPanel form, String name, JComponent component) {
        JLabel label = new JLabel(name);
        label.setLabelFor(component);
        form.add(label);
        form.add(component);
    }

    private static ListCellRenderer<Object> cityRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                JLabel cell = (JLabel) super.getListCellRendererComponent(list,
                        value instanceof City city ? city.name() : value, index, selected, focus);
                cell.setToolTipText(value instanceof City city ? city.name() : null);
                return cell;
            }
        };
    }

    private static JPanel card(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        panel.add(label(title, 17, true, INK), BorderLayout.NORTH);
        return panel;
    }

    private static JScrollPane scroll(JComponent component) {
        JScrollPane pane = new JScrollPane(component);
        pane.setBorder(BorderFactory.createLineBorder(LINE));
        return pane;
    }

    private static JPanel actions(JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 8));
        panel.setOpaque(false);
        for (JButton button : buttons) panel.add(button);
        return panel;
    }

    private static JPanel stack(int gap) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, gap));
        panel.setOpaque(false);
        return panel;
    }

    private static JLabel label(String value, int size, boolean bold, Color color) {
        JLabel label = new JLabel(value);
        label.setFont(new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    private static JTextArea textArea(int fontSize) {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        area.setForeground(INK);
        return area;
    }

    private static final class ActionButton extends JButton {
        private final boolean primary;

        private ActionButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            setForeground(primary ? Color.WHITE : INK);
            setMargin(new Insets(8, 12, 8, 12));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D background = (Graphics2D) graphics.create();
            try {
                background.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = primary ? ACCENT : Color.WHITE;
                if (!isEnabled()) {
                    fill = new Color(231, 237, 240);
                } else if (getModel().isPressed()) {
                    fill = primary ? ACCENT.darker() : LINE;
                } else if (getModel().isRollover()) {
                    fill = primary ? new Color(13, 88, 73) : new Color(243, 248, 249);
                }
                int width = getWidth() - 3;
                int height = getHeight() - 3;
                background.setColor(fill);
                background.fillRoundRect(1, 1, width, height, 10, 10);
                background.setColor(isFocusOwner() ? INK : primary && isEnabled() ? fill : LINE);
                background.setStroke(new BasicStroke(isFocusOwner() ? 2 : 1));
                background.drawRoundRect(1, 1, width, height, 10, 10);
            } finally {
                background.dispose();
            }
            super.paintComponent(graphics);
        }
    }

    private static final class ResultView extends JPanel {
        private final JPanel comparison = stack(3);
        private final JLabel cost = label("", 14, true, ACCENT);
        private final JLabel elapsed = label("", 12, false, MUTED);
        private final JLabel routeTitle = label("", 14, true, INK);
        private final JTextArea route = textArea(14);
        private final JTextArea details = textArea(13);
        private List<City> bestRoute = List.of();

        private ResultView(Algorithm algorithm) {
            super(new BorderLayout(0, 10));
            comparison.setOpaque(true);
            comparison.setBackground(new Color(235, 245, 241));
            comparison.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            comparison.add(label(algorithm.label, 13, true, INK));
            comparison.add(cost);
            comparison.add(elapsed);
            elapsed.setToolTipText("Tempo da busca, incluindo o registro dos passos.");

            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(12, 8, 8, 8));
            JPanel summary = new JPanel(new BorderLayout(0, 6));
            summary.setBackground(new Color(245, 249, 250));
            summary.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            summary.add(routeTitle, BorderLayout.NORTH);
            route.setOpaque(false);
            route.setRows(2);
            JScrollPane routeScroll = new JScrollPane(route);
            routeScroll.setOpaque(false);
            routeScroll.getViewport().setOpaque(false);
            routeScroll.setBorder(null);
            summary.add(routeScroll, BorderLayout.CENTER);
            add(summary, BorderLayout.NORTH);
            details.setMargin(new Insets(8, 8, 8, 8));
            add(scroll(details), BorderLayout.CENTER);
            clear();
        }

        private void clear() {
            showMessage("Sem resultado", "Escolha a origem e clique em Calcular rota.");
        }

        private void showMessage(String title, String message) {
            bestRoute = List.of();
            cost.setText("Custo —");
            elapsed.setText("Tempo —");
            routeTitle.setText(title);
            route.setText(message);
            route.setCaretPosition(0);
            details.setText("");
        }

        private void showResult(RunOutput output) {
            TspResult result = output.result();
            bestRoute = result.bestRoute();
            cost.setText(result.hasTour() ? "Custo " + result.bestCost().getAsLong() : "Sem ciclo válido");
            elapsed.setText("Tempo " + formatElapsed(output.elapsedNanos()));
            routeTitle.setText(result.hasTour() ? "Melhor rota" : "Sem ciclo válido");
            route.setText(result.hasTour() ? formatRoute(result.bestRoute())
                    : "O grafo não permite visitar todas as cidades e voltar à origem.");
            route.setCaretPosition(0);
            details.setText(output.details());
            details.setCaretPosition(0);
        }
    }

    private static String formatElapsed(long nanos) {
        Locale locale = Locale.forLanguageTag("pt-BR");
        if (nanos < 1_000_000) return String.format(locale, "%.1f µs", nanos / 1_000.0);
        if (nanos < 1_000_000_000) return String.format(locale, "%.2f ms", nanos / 1_000_000.0);
        return String.format(locale, "%.2f s", nanos / 1_000_000_000.0);
    }

    private record RunOutput(TspResult result, long elapsedNanos, String details) {}

    private enum Algorithm {
        BRUTE_FORCE("Força bruta"),
        DIJKSTRA("Dijkstra adaptado");

        private final String label;

        Algorithm(String label) {
            this.label = label;
        }
    }
}
