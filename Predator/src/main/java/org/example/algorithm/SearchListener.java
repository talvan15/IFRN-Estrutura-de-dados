package org.example.algorithm;
import org.example.model.*;
public interface SearchListener {
    SearchListener NONE=new SearchListener() {
    }
    ;
    default void onSegmentStarted() {
    }
    default void onNodeOpened(Node node,int openSize) {
    }
    default void onNodeClosed(Node node,int explored,long algorithmNs) {
    }
    default void onPathFound(SearchResult result) {
    }
}
