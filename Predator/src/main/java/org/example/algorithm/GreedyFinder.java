package org.example.algorithm;
import org.example.model.Node;
public final class GreedyFinder extends BestFirstFinder {
    protected double priority(Node node) {
        return node.h();
    }
}
