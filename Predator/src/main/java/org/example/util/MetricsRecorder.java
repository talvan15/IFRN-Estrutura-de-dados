package org.example.util;
import org.example.model.SearchResult;
import java.util.*;
import java.nio.file.*;
import java.io.*;
public final class MetricsRecorder {
    public record Run(String algorithm,String heuristic,SearchResult result) {
    }
    private static final int HISTORY_LIMIT=100;
    private final List<Run> runs=new ArrayList<>();
    public void add(Run run) {
        runs.add(run);
        if(runs.size()>HISTORY_LIMIT)runs.removeFirst();
    }
    public List<Run> runs() {
        return List.copyOf(runs);
    }
    public void export(Path file)throws IOException {
        StringBuilder csv=new StringBuilder("algoritmo,heuristica,tempo_ns,nos_explorados,nos_gerados,custo,passos,max_aberta,encontrado\n");
        for(Run r:runs) {
            SearchResult s=r.result;
            csv.append(r.algorithm).append(',').append(r.heuristic).append(',').append(s.timeNs()).append(',').append(s.exploredNodes()).append(',').append(s.generatedNodes()).append(',').append(s.found()?s.totalCost():"").append(',').append(s.steps()).append(',').append(s.maxOpenSize()).append(',').append(s.found()).append('\n');
        }
        Files.writeString(file,csv);
    }
}
