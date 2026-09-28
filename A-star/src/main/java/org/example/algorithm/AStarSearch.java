package org.example.algorithm;

import org.example.model.Grid;
import org.example.model.Node;
import org.example.model.Position;

import java.util.*;

public class AStarSearch implements SearchAlgorithm {

    @Override
    public SearchResult search(Grid grid) {

        long startTime = System.nanoTime();

        Position start = grid.getStart();
        Position goal = grid.getGoal();

        List<SearchStep> steps = new ArrayList<>();

        if (start == null || goal == null) {
            return emptyResult(steps, startTime);
        }

        Map<Position, Node> nodes = new HashMap<>();

        Map<Position, Double> gValues = new HashMap<>();
        Map<Position, Double> hValues = new HashMap<>();

        PriorityQueue<Node> openQueue =
                new PriorityQueue<>(
                        Comparator
                                .comparingDouble(Node::getF)
                                .thenComparingDouble(Node::getH)
                );

        Set<Position> openPositions = new HashSet<>();
        Set<Position> closed = new HashSet<>();

        Node startNode = new Node(start);

        startNode.setG(0);
        startNode.setH(heuristic(start, goal));

        nodes.put(start, startNode);

        gValues.put(start, startNode.getG());
        hValues.put(start, startNode.getH());

        openQueue.add(startNode);
        openPositions.add(start);

        Node goalNode = null;

        while (!openQueue.isEmpty()) {

            Node current = openQueue.poll();

            if (closed.contains(current.getPosition())) {
                continue;
            }

            openPositions.remove(current.getPosition());

            if (current.getPosition().equals(goal)) {

                goalNode = current;

                steps.add(
                        createStep(
                                openPositions,
                                closed,
                                current.getPosition(),
                                gValues,
                                hValues
                        )
                );

                break;
            }

            closed.add(current.getPosition());

            for (Position neighborPosition :
                    grid.getNeighbors(current.getPosition())) {

                if (closed.contains(neighborPosition)) {
                    continue;
                }

                double tentativeG =
                        current.getG() + 1;

                Node neighbor =
                        nodes.computeIfAbsent(
                                neighborPosition,
                                Node::new
                        );

                if (tentativeG < neighbor.getG()) {

                    neighbor.setParent(current);
                    neighbor.setG(tentativeG);
                    neighbor.setH(
                            heuristic(
                                    neighborPosition,
                                    goal
                            )
                    );

                    gValues.put(
                            neighborPosition,
                            neighbor.getG()
                    );

                    hValues.put(
                            neighborPosition,
                            neighbor.getH()
                    );

                    /*
                     * PriorityQueue não reorganiza
                     * automaticamente um elemento cuja
                     * prioridade interna foi modificada.
                     */
                    openQueue.remove(neighbor);
                    openQueue.add(neighbor);

                    openPositions.add(
                            neighborPosition
                    );
                }
            }

            steps.add(
                    createStep(
                            openPositions,
                            closed,
                            current.getPosition(),
                            gValues,
                            hValues
                    )
            );
        }

        List<Position> path =
                goalNode == null
                        ? Collections.emptyList()
                        : reconstructPath(goalNode);

        long endTime = System.nanoTime();

        return new SearchResult(
                getName(),
                goalNode != null,
                path,
                steps,
                closed.size(),
                goalNode == null
                        ? 0
                        : goalNode.getG(),
                (endTime - startTime)
                        / 1_000_000.0
        );
    }

    private SearchStep createStep(
            Set<Position> open,
            Set<Position> closed,
            Position current,
            Map<Position, Double> gValues,
            Map<Position, Double> hValues
    ) {

        return new SearchStep(
                open,
                closed,
                current,
                gValues,
                hValues
        );
    }

    private double heuristic(
            Position a,
            Position b
    ) {

        return Math.abs(
                a.getRow() - b.getRow()
        )
                +
                Math.abs(
                        a.getColumn()
                                - b.getColumn()
                );
    }

    private List<Position> reconstructPath(
            Node goal
    ) {

        List<Position> path =
                new ArrayList<>();

        Node current = goal;

        while (current != null) {

            path.add(
                    current.getPosition()
            );

            current = current.getParent();
        }

        Collections.reverse(path);

        return path;
    }

    private SearchResult emptyResult(
            List<SearchStep> steps,
            long startTime
    ) {

        long endTime = System.nanoTime();

        return new SearchResult(
                getName(),
                false,
                Collections.emptyList(),
                steps,
                0,
                0,
                (endTime - startTime)
                        / 1_000_000.0
        );
    }

    @Override
    public String getName() {
        return "A*";
    }
}