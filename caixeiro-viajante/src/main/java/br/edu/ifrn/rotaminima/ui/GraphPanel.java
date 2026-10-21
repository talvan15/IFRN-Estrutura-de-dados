package br.edu.ifrn.rotaminima.ui;

import br.edu.ifrn.rotaminima.model.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;
import javax.swing.*;

public final class GraphPanel extends JPanel {
  public enum EditMode {
    ADD_CITY,
    CONNECT,
    REMOVE_EDGE
  }

  public interface Editor {
    void addCity(double x, double y);

    void editEdge(int a, int b, boolean remove);
  }

  private Graph graph;
  private final Editor editor;
  private EditMode mode = EditMode.ADD_CITY;
  private int pending = -1, source = -1, target = -1;
  private List<Integer> path = List.of();
  private boolean editing = true;
  private static final int RADIUS = 21;
  private static final Color BLUE = new Color(35, 100, 200), GREEN = new Color(28, 142, 95);

  public GraphPanel(Graph graph, Editor editor) {
    this.graph = graph;
    this.editor = editor;
    setBackground(new Color(247, 249, 252));
    setPreferredSize(new Dimension(720, 650));
    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent event) {
            if (!editing) return;
            int hit = hit(event.getX(), event.getY());
            if (mode == EditMode.ADD_CITY) {
              if (hit < 0) {
                Point2D p = toModel(event.getX(), event.getY());
                if (p.getX() < 0 || p.getX() > 1000 || p.getY() < 0 || p.getY() > 800) return;
                for (City c : graph.cities())
                  if (toScreen(c.x(), c.y()).distance(event.getPoint()) < RADIUS * 2 + 12) return;
                editor.addCity(p.getX(), p.getY());
              }
            } else if (hit >= 0) {
              if (pending < 0) {
                pending = hit;
                repaint();
              } else {
                int first = pending;
                pending = -1;
                repaint();
                if (first != hit) editor.editEdge(first, hit, mode == EditMode.REMOVE_EDGE);
              }
            } else {
              pending = -1;
              repaint();
            }
          }
        });
  }

  public void setGraph(Graph graph) {
    this.graph = graph;
    clear();
  }

  public void setMode(EditMode mode) {
    this.mode = mode;
    pending = -1;
    repaint();
  }

  public void setEditing(boolean editing) {
    this.editing = editing;
    pending = -1;
    repaint();
  }

  public void clear() {
    path = List.of();
    source = target = -1;
    pending = -1;
    repaint();
  }

  public void showRoute(int source, int target, List<Integer> path) {
    this.source = source;
    this.target = target;
    this.path = List.copyOf(path);
    repaint();
  }

  private double scale() {
    return Math.max(
        .01,
        Math.min(Math.max(1, getWidth() - 90) / 1000.0, Math.max(1, getHeight() - 110) / 800.0));
  }

  private Point2D toScreen(double x, double y) {
    double s = scale();
    return new Point2D.Double(
        (getWidth() - 1000 * s) / 2 + x * s, (getHeight() - 35 - 800 * s) / 2 + y * s);
  }

  private Point2D toModel(double x, double y) {
    double s = scale();
    Point2D offset = toScreen(0, 0);
    return new Point2D.Double((x - offset.getX()) / s, (y - offset.getY()) / s);
  }

  private int hit(double x, double y) {
    for (int i = 0; i < graph.size(); i++)
      if (toScreen(graph.city(i).x(), graph.city(i).y()).distance(x, y) <= RADIUS + 3) return i;
    return -1;
  }

  private boolean highlighted(int a, int b) {
    for (int i = 1; i < path.size(); i++)
      if ((path.get(i - 1) == a && path.get(i) == b) || (path.get(i - 1) == b && path.get(i) == a))
        return true;
    return false;
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    Graphics2D g = (Graphics2D) graphics.create();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setFont(getFont().deriveFont(Font.BOLD, 13f));
    for (int a = 0; a < graph.size(); a++)
      for (int b = a + 1; b < graph.size(); b++)
        if (Double.isFinite(graph.weight(a, b))) {
          Point2D p = toScreen(graph.city(a).x(), graph.city(a).y()),
              q = toScreen(graph.city(b).x(), graph.city(b).y());
          boolean marked = highlighted(a, b);
          g.setColor(marked ? BLUE : new Color(165, 178, 195));
          g.setStroke(new BasicStroke(marked ? 4 : 1.5f));
          g.draw(new Line2D.Double(p, q));
          String label = format(graph.weight(a, b));
          int x = (int) (p.getX() * .58 + q.getX() * .42),
              y = (int) (p.getY() * .58 + q.getY() * .42);
          int width = g.getFontMetrics().stringWidth(label);
          g.setColor(new Color(255, 255, 255, 240));
          g.fillRoundRect(x - width / 2 - 6, y - 12, width + 12, 23, 8, 8);
          g.setColor(new Color(39, 52, 72));
          g.drawString(label, x - width / 2, y + 5);
        }
    for (int i = 0; i < graph.size(); i++) {
      City c = graph.city(i);
      Point2D p = toScreen(c.x(), c.y());
      int x = (int) p.getX(), y = (int) p.getY();
      g.setColor(i == source ? GREEN : i == target ? new Color(184, 94, 38) : BLUE);
      g.fillOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);
      if (i == pending) {
        g.setColor(new Color(242, 172, 41));
        g.setStroke(new BasicStroke(3));
        g.drawOval(x - RADIUS - 4, y - RADIUS - 4, 2 * RADIUS + 8, 2 * RADIUS + 8);
      }
      g.setColor(Color.WHITE);
      g.drawString(c.name(), x - g.getFontMetrics().stringWidth(c.name()) / 2, y + 5);
    }
    g.setFont(getFont().deriveFont(11f));
    g.setColor(new Color(80, 94, 115));
    g.drawString(
        "Verde: origem   •   Laranja: destino   •   Azul nas arestas: trajeto   •   Pesos"
            + " independem da posição",
        16,
        getHeight() - 15);
    if (graph.size() == 0) {
      g.setFont(getFont().deriveFont(17f));
      g.drawString("Clique para adicionar cidades ou carregue um exemplo.", 35, 70);
    }
    g.dispose();
  }

  public static String format(double value) {
    // Preserva pesos muito pequenos: arredondá-los para zero ocultaria a aresta real.
    return Double.isFinite(value)
        ? java.math.BigDecimal.valueOf(value).stripTrailingZeros().toString().replace('.', ',')
        : "∞";
  }
}
