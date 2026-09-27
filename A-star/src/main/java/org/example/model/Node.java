package org.example.model;

public class Node {

    private final Position position;

    private double g;
    private double h;

    private Node parent;

    public Node(Position position) {
        this.position = position;
        this.g = Double.POSITIVE_INFINITY;
        this.h = 0;
    }

    public Position getPosition() {
        return position;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public double getF() {
        return g + h;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}