package br.edu.ifrn.rotaminima.ui;

import br.edu.ifrn.rotaminima.model.*;
import br.edu.ifrn.rotaminima.service.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.table.*;

public final class MainFrame extends JFrame implements GraphPanel.Editor {
  private Graph graph = new Graph();
  private final GraphPanel canvas = new GraphPanel(graph, this);
  private final RoutePlanningService service = new RoutePlanningService();
  private final JComboBox<String> editMode =
      new JComboBox<>(
          new String[] {"Adicionar cidade", "Conectar / atualizar peso", "Remover aresta"});
  private final JComboBox<String> examples = new JComboBox<>(GraphExamples.NAMES);
  private final JButton load = new JButton("Carregar exemplo"), clear = new JButton("Limpar");
  private final JComboBox<City> source = new JComboBox<>(),
      target = new JComboBox<>(),
      start = new JComboBox<>();
  private final JButton shortest = new JButton("Calcular caminho"),
      tour = new JButton("Calcular rota"),
      previous = new JButton("Anterior"),
      next = new JButton("Próximo");
  private final JTextArea pathText = output(4), tourText = output(10);
  private final JLabel stepLabel = new JLabel("Sem cálculo"),
      status = new JLabel("Pronto. Adicione cidades ou carregue um exemplo.");
  private final DefaultTableModel tableModel =
      new DefaultTableModel(new String[] {"Cidade", "Distância", "Predecessor", "Finalizada"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
          return false;
        }
      };
  private final JTabbedPane tabs = new JTabbedPane();
  private DijkstraResult result;
  private TourResult tourResult;
  private int stepIndex = -1;
  private boolean busy;

  public MainFrame() {
    super("RotaMinima • IFRN | Estrutura de Dados Não Lineares");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 800);
    setMinimumSize(new Dimension(1000, 650));
    setLocationRelativeTo(null);
    setLayout(new BorderLayout(10, 10));
    JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
    toolbar.add(new JLabel("Edição:"));
    toolbar.add(editMode);
    toolbar.add(examples);
    toolbar.add(load);
    toolbar.add(clear);
    JPanel header = new JPanel(new BorderLayout());
    header.add(toolbar, BorderLayout.NORTH);
    JLabel help =
        new JLabel(
            "  Adicionar: clique no espaço vazio. Conectar/remover: clique em duas cidades. Máximo:"
                + " 20 cidades.");
    help.setBorder(BorderFactory.createEmptyBorder(0, 6, 8, 6));
    header.add(help, BorderLayout.SOUTH);
    add(header, BorderLayout.NORTH);
    tabs.addTab("Dijkstra", createDijkstraTab());
    JScrollPane tourScroll = new JScrollPane(createTourTab());
    tourScroll.setBorder(BorderFactory.createEmptyBorder());
    tourScroll.getVerticalScrollBar().setUnitIncrement(16);
    tabs.addTab("Rota de visitas", tourScroll);
    tabs.setPreferredSize(new Dimension(425, 600));
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, tabs);
    split.setResizeWeight(.65);
    split.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    add(split, BorderLayout.CENTER);
    status.setBorder(BorderFactory.createEmptyBorder(8, 14, 10, 10));
    add(status, BorderLayout.SOUTH);
    editMode.addActionListener(
        e -> canvas.setMode(GraphPanel.EditMode.values()[editMode.getSelectedIndex()]));
    load.addActionListener(
        e -> {
          graph = GraphExamples.create(examples.getSelectedIndex());
          changed();
          status.setText("Exemplo carregado: " + examples.getSelectedItem());
        });
    clear.addActionListener(
        e -> {
          graph = new Graph();
          changed();
          status.setText("Grafo limpo.");
        });
    shortest.addActionListener(e -> calculateShortest());
    tour.addActionListener(e -> calculateTour());
    previous.addActionListener(
        e -> {
          stepIndex--;
          showStep();
        });
    next.addActionListener(
        e -> {
          stepIndex++;
          showStep();
        });
    tabs.addChangeListener(e -> refreshHighlight());
    source.addActionListener(e -> selectionChanged());
    target.addActionListener(e -> selectionChanged());
    start.addActionListener(e -> selectionChanged());
    changed();
  }

  private JPanel column() {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
    return p;
  }

  private JPanel row(Component... components) {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 5));
    for (Component c : components) p.add(c);
    p.setAlignmentX(Component.LEFT_ALIGNMENT);
    p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
    return p;
  }

  private JLabel paragraph(String text) {
    JLabel label = new JLabel("<html><div style='width:300px'>" + text + "</div></html>");
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    return label;
  }

  private JPanel createDijkstraTab() {
    JPanel p = column();
    p.add(
        paragraph(
            "<b>Caminho mínimo entre duas cidades</b><br>Dijkstra • seleção linear • O(V²) por"
                + " origem"));
    p.add(row(new JLabel("Origem:"), source, new JLabel("Destino:"), target));
    p.add(row(shortest));
    p.add(scroll(pathText));
    p.add(row(previous, next, stepLabel));
    JTable table = new JTable(tableModel);
    table.setRowHeight(25);
    table.setFillsViewportHeight(true);
    JScrollPane scroll = new JScrollPane(table);
    scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
    p.add(scroll);
    p.add(Box.createVerticalStrut(10));
    p.add(
        paragraph(
            "Cada etapa mostra os vetores após finalizar e relaxar uma cidade. ∞ indica cidade"
                + " ainda não alcançada. No último passo, o caminho final fica destacado."));
    return p;
  }

  private JPanel createTourTab() {
    JPanel p = column();
    p.add(
        paragraph(
            "<b>Percurso fechado de menor custo</b><br>Dijkstra por cidade + força bruta • 3 a 9"
                + " cidades"));
    p.add(row(new JLabel("Cidade inicial:"), start));
    p.add(row(tour));
    p.add(scroll(tourText));
    p.add(Box.createVerticalStrut(12));
    p.add(
        paragraph(
            "A ordem planejada contém cada cidade uma vez, mas os caminhos entre elas podem"
                + " atravessar outras cidades e repetir vértices ou arestas."));
    p.add(Box.createVerticalStrut(12));
    p.add(
        paragraph(
            "Dijkstra calcula os menores trechos. A força bruta escolhe a ordem e inclui o retorno"
                + " à origem. O resultado não garante um ciclo hamiltoniano no grafo original."));
    p.add(Box.createVerticalStrut(12));
    p.add(Box.createVerticalStrut(12));
    p.add(
        paragraph(
            "No exemplo de quatro cidades, os pesos do enunciado produzem custo 10 com repetições"
                + " (A → B → C → D → B → A). O valor 11 corresponde ao ciclo direto sem"
                + " repetições."));
    p.add(
        paragraph(
            "O tempo informado mede uma execução, sem desenho ou navegação; não é um benchmark"
                + " rigoroso."));
    return p;
  }

  private static JTextArea output(int rows) {
    JTextArea area = new JTextArea(rows, 25);
    area.setEditable(false);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
    area.setMargin(new Insets(10, 10, 10, 10));
    return area;
  }

  private JScrollPane scroll(JTextArea area) {
    JScrollPane s = new JScrollPane(area);
    s.setAlignmentX(Component.LEFT_ALIGNMENT);
    s.setPreferredSize(new Dimension(380, area == pathText ? 135 : 290));
    s.setMaximumSize(new Dimension(Integer.MAX_VALUE, area == pathText ? 160 : 340));
    return s;
  }

  private void changed() {
    result = null;
    tourResult = null;
    stepIndex = -1;
    pathText.setText("");
    tourText.setText("");
    tableModel.setRowCount(0);
    stepLabel.setText("Sem cálculo");
    canvas.setGraph(graph);
    for (JComboBox<City> combo : List.of(source, target, start)) {
      combo.removeAllItems();
      for (City city : graph.cities()) combo.addItem(city);
    }
    if (graph.size() > 1) target.setSelectedIndex(graph.size() - 1);
    updateControls();
    refreshHighlight();
  }

  private void selectionChanged() {
    if (busy) return;
    result = null;
    tourResult = null;
    stepIndex = -1;
    pathText.setText("");
    tourText.setText("");
    tableModel.setRowCount(0);
    stepLabel.setText("Sem cálculo");
    updateControls();
    refreshHighlight();
  }

  private void updateControls() {
    for (Component c : List.of(editMode, examples, load, clear, source, target, start, tabs))
      c.setEnabled(!busy);
    canvas.setEditing(!busy);
    shortest.setEnabled(!busy && graph.size() > 0);
    tour.setEnabled(!busy && graph.size() >= 3 && graph.size() <= 9);
    previous.setEnabled(!busy && result != null && stepIndex > 0);
    next.setEnabled(!busy && result != null && stepIndex < result.steps().size() - 1);
  }

  @Override
  public void addCity(double x, double y) {
    if (busy) return;
    if (graph.size() >= 20) {
      message("A interface permite no máximo 20 cidades.");
      return;
    }
    graph.addCity("" + (char) ('A' + graph.size()), x, y);
    changed();
    status.setText("Cidade adicionada. Resultados anteriores invalidados.");
  }

  @Override
  public void editEdge(int a, int b, boolean remove) {
    if (busy) return;
    if (remove) {
      if (!Double.isFinite(graph.weight(a, b))) {
        message("Não existe aresta entre essas cidades.");
        return;
      }
      graph.removeEdge(a, b);
      changed();
      status.setText("Aresta removida.");
      return;
    }
    String value =
        JOptionPane.showInputDialog(
            this,
            "Peso entre " + graph.city(a) + " e " + graph.city(b) + " (finito e não negativo):",
            Double.isFinite(graph.weight(a, b)) ? Double.toString(graph.weight(a, b)) : "1");
    if (value == null) return;
    try {
      graph.setEdge(a, b, Double.parseDouble(value.trim().replace(',', '.')));
      changed();
      status.setText("Peso atualizado. Resultados anteriores invalidados.");
    } catch (IllegalArgumentException ex) {
      message("Peso inválido. Digite um número finito maior ou igual a zero.");
    }
  }

  private void calculateShortest() {
    int s = source.getSelectedIndex(), t = target.getSelectedIndex();
    Graph copy = graph.copy();
    run(
        () -> service.shortestPath(copy, s),
        value -> {
          result = value;
          tourResult = null;
          stepIndex = 0;
          pathText.setText(
              value.reachable(t)
                  ? "Caminho: "
                      + names(value.cities(), value.pathTo(t))
                      + "\nCusto: "
                      + GraphPanel.format(value.distanceTo(t))
                  : "Destino inacessível a partir da origem.");
          pathText.append("\nTempo desta execução: " + elapsedText);
          showStep();
        });
  }

  private void calculateTour() {
    int s = start.getSelectedIndex();
    Graph copy = graph.copy();
    run(
        () -> service.visitingRoute(copy, s),
        value -> {
          tourResult = value;
          result = null;
          if (!value.possible())
            tourText.setText(
                "Não existe um percurso fechado capaz de visitar todas as cidades: grafo"
                    + " desconectado.");
          else
            tourText.setText(
                "Ordem planejada (com retorno):\n"
                    + names(value.cities(), value.plannedOrder())
                    + "\n\nTrajeto expandido no grafo original:\n"
                    + names(value.cities(), value.expandedPath())
                    + "\n\nCusto total: "
                    + GraphPanel.format(value.cost()));
          tourText.append(
              "\nOrdens avaliadas: "
                  + value.evaluatedOrders()
                  + "\nTempo total desta execução: "
                  + elapsedText);
          refreshHighlight();
        });
  }

  private String elapsedText;

  private <T> void run(Supplier<T> task, java.util.function.Consumer<T> onSuccess) {
    selectionChanged();
    busy = true;
    canvas.clear();
    status.setText("Calculando…");
    updateControls();
    new SwingWorker<T, Void>() {
      long elapsed;

      @Override
      protected T doInBackground() {
        long began = System.nanoTime();
        try {
          return task.get();
        } finally {
          elapsed = System.nanoTime() - began;
        }
      }

      @Override
      protected void done() {
        try {
          T value = get();
          elapsedText =
              String.format(
                  java.util.Locale.forLanguageTag("pt-BR"), "%.3f ms", elapsed / 1_000_000.0);
          onSuccess.accept(value);
          status.setText("Cálculo concluído. Tempo de uma execução; não é benchmark.");
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          message("Cálculo interrompido.");
          status.setText("Cálculo interrompido.");
        } catch (ExecutionException | RuntimeException ex) {
          Throwable cause = ex.getCause() == null ? ex : ex.getCause();
          message("Não foi possível calcular: " + cause.getMessage());
          status.setText("Falha no cálculo.");
        } finally {
          busy = false;
          updateControls();
          refreshHighlight();
        }
      }
    }.execute();
  }

  private void showStep() {
    if (result == null || stepIndex < 0 || stepIndex >= result.steps().size()) return;
    DijkstraStep step = result.steps().get(stepIndex);
    double[] distances = step.distances();
    int[] predecessors = step.predecessors();
    boolean[] finalized = step.finalized();
    tableModel.setRowCount(0);
    for (int i = 0; i < distances.length; i++)
      tableModel.addRow(
          new Object[] {
            result.cities().get(i).name(),
            GraphPanel.format(distances[i]),
            predecessors[i] < 0 ? "—" : result.cities().get(predecessors[i]).name(),
            finalized[i] ? "Sim" : "Não"
          });
    stepLabel.setText(
        (stepIndex + 1)
            + "/"
            + result.steps().size()
            + " • "
            + result.cities().get(step.processed()).name());
    updateControls();
    refreshHighlight();
  }

  private void refreshHighlight() {
    if (busy) return;
    int s = tabs.getSelectedIndex() == 0 ? source.getSelectedIndex() : start.getSelectedIndex();
    int t = tabs.getSelectedIndex() == 0 ? target.getSelectedIndex() : -1;
    List<Integer> path = List.of();
    if (tabs.getSelectedIndex() == 0 && result != null && stepIndex == result.steps().size() - 1)
      path = result.pathTo(t);
    if (tabs.getSelectedIndex() == 1 && tourResult != null && tourResult.possible())
      path = tourResult.expandedPath();
    canvas.showRoute(s, t, path);
  }

  private String names(List<City> cities, List<Integer> indices) {
    List<String> names = new ArrayList<>();
    for (int i : indices) names.add(cities.get(i).name());
    return String.join(" → ", names);
  }

  private void message(String text) {
    JOptionPane.showMessageDialog(this, text, "RotaMinima", JOptionPane.INFORMATION_MESSAGE);
  }
}
