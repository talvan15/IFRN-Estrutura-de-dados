package org.example.algorithm;
import org.example.model.Node;
public final class AStarFinder extends BestFirstFinder {
    protected double priority(Node node) {
        return node.f();
    }
}
