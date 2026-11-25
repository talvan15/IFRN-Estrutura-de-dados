package org.example.ui;

import org.example.model.City;
import org.example.model.Edge;
import org.example.model.Graph;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Draws the city graph and highlights the selected origin and best tour. */
@SuppressWarnings("serial")
public final class GraphPanel extends JPanel {
    private static final Color EDGE_COLOR = new Color(161, 175, 185);
    static final Color ROUTE_COLOR = new Color(161, 67, 23);
    private static final Color CITY_COLOR = new Color(36, 64, 86);
    static final Color ORIGIN_COLOR = new Color(17, 105, 87);
    private Graph graph;
    private City origin;
    private List<City> highlightedRoute = List.of();

    public GraphPanel(Graph graph) {
        this.graph = graph;
        setBackground(new Color(245, 249, 250));
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        setPreferredSize(new java.awt.Dimension(640, 560));
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        this.highlightedRoute = List.of();
        repaint();
    }

    public void showOrigin(City origin) {
        this.origin = origin;
        repaint();
    }

    public void showRoute(List<City> route) {
        highlightedRoute = List.copyOf(route);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            List<City> cities = graph.cities();
            Point2D.Double[] points = positions(cities.size(), getWidth(), getHeight());
            Set<String> routeEdges = routeEdges(highlightedRoute);
            Map<City, Point2D.Double> cityPoints = new HashMap<>();
            for (int i = 0; i < cities.size(); i++) cityPoints.put(cities.get(i), points[i]);

            g.setColor(new Color(223, 231, 235));
            for (int x = 14; x < getWidth(); x += 20) {
                for (int y = 14; y < getHeight(); y += 20) g.fillOval(x, y, 2, 2);
            }

            List<Edge> edges = graph.edges();
            for (Edge edge : edges) {
                Point2D.Double first = cityPoints.get(edge.first());
                Point2D.Double second = cityPoints.get(edge.second());
                boolean highlighted = routeEdges.contains(edgeKey(edge.first(), edge.second()));
                g.setColor(highlighted ? ROUTE_COLOR : EDGE_COLOR);
                g.setStroke(new BasicStroke(highlighted ? 4f : 2f));
                g.drawLine((int) first.x, (int) first.y, (int) second.x, (int) second.y);
            }
            g.setFont(getFont().deriveFont(Font.BOLD, 12f));
            for (Edge edge : edges) {
                drawWeight(g, edge.weight(), cityPoints.get(edge.first()), cityPoints.get(edge.second()));
            }

            for (int i = 0; i < cities.size(); i++) {
                City city = cities.get(i);
                Point2D.Double point = points[i];
                boolean isOrigin = city.equals(origin);
                g.setColor(isOrigin ? ORIGIN_COLOR : CITY_COLOR);
                g.setFont(getFont().deriveFont(Font.BOLD, 13f));
                FontMetrics metrics = g.getFontMetrics();
                String name = clipName(city.name(), metrics);
                int textWidth = metrics.stringWidth(name);
                int nodeWidth = Math.max(40, textWidth + 22);
                g.fillRoundRect((int) point.x - nodeWidth / 2, (int) point.y - 19, nodeWidth, 38, 26, 26);
                g.setColor(Color.WHITE);
                g.drawString(name, (int) point.x - textWidth / 2,
                        (int) point.y + (metrics.getAscent() - metrics.getDescent()) / 2);
                if (isOrigin) {
                    g.setColor(ORIGIN_COLOR.darker());
                    g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
                    g.drawString("Origem", (int) point.x - 22, (int) point.y + 42);
                }
            }
        } finally {
            g.dispose();
        }
    }

    private static String clipName(String name, FontMetrics metrics) {
        if (metrics.stringWidth(name) <= 94) return name;
        int end = 0;
        while (end < name.length()) {
            int next = name.offsetByCodePoints(end, 1);
            if (metrics.stringWidth(name.substring(0, next) + "…") > 94) break;
            end = next;
        }
        return name.substring(0, end) + "…";
    }

    private static Point2D.Double[] positions(int count, int width, int height) {
        Point2D.Double[] points = new Point2D.Double[count];
        if (count == 0) return points;
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double radius = count == 1 ? 0 : Math.max(0, Math.min(width - 124, height - 92) * 0.44);
        for (int i = 0; i < count; i++) {
            double angle = -Math.PI / 2 + (2 * Math.PI * i / count);
            points[i] = new Point2D.Double(centerX + radius * Math.cos(angle),
                    centerY + radius * Math.sin(angle));
        }
        return points;
    }

    private static void drawWeight(Graphics2D g, int weight, Point2D.Double first, Point2D.Double second) {
        double midX = first.x * 0.58 + second.x * 0.42;
        double midY = first.y * 0.58 + second.y * 0.42;
        double dx = second.x - first.x;
        double dy = second.y - first.y;
        double length = Math.max(1, Math.hypot(dx, dy));
        int x = (int) (midX - dy / length * 24);
        int y = (int) (midY + dx / length * 24);
        String label = Integer.toString(weight);
        FontMetrics metrics = g.getFontMetrics();
        int labelWidth = metrics.stringWidth(label);
        g.setColor(new Color(255, 255, 255, 225));
        g.fillRoundRect(x - labelWidth / 2 - 4, y - metrics.getAscent(), labelWidth + 8,
                metrics.getHeight(), 7, 7);
        g.setColor(new Color(40, 45, 52));
        g.drawString(label, x - labelWidth / 2, y);
    }

    private static Set<String> routeEdges(List<City> route) {
        Set<String> keys = new HashSet<>();
        for (int i = 1; i < route.size(); i++) {
            keys.add(edgeKey(route.get(i - 1), route.get(i)));
        }
        return keys;
    }

    private static String edgeKey(City first, City second) {
        return first.name().compareTo(second.name()) <= 0
                ? first.name() + "\u0000" + second.name()
                : second.name() + "\u0000" + first.name();
    }
}
