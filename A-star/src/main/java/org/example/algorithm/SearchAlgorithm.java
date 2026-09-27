package org.example.algorithm;

import org.example.model.Grid;

public interface SearchAlgorithm {

    SearchResult search(Grid grid);

    String getName();
}