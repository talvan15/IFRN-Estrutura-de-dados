package org.example.util;
import org.example.model.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
public final class MazeLoader {
    public record Scenario(Grid grid,Position start,List<Position> goals) {
        public Scenario {
            goals=List.copyOf(goals);
        }
    }
    public static Scenario read(InputStream input)throws IOException {
        if(input==null) throw new IOException("Mapa não encontrado");
        List<String> lines;
        try(var reader=new BufferedReader(new InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8))) {
            lines=reader.lines().toList();
        }
        if(lines.isEmpty())throw new IOException("Mapa vazio");
        String metadata=lines.getLast().startsWith("@goals ")?lines.getLast():null;
        if(metadata!=null)lines=lines.subList(0,lines.size()-1);
        if(lines.size()<10||lines.size()>80||lines.getFirst().length()<10||lines.getFirst().length()>80)throw new IOException("Dimensões devem estar entre 10 e 80");
        Grid grid=new Grid(lines.getFirst().length(),lines.size());
        Position start=null;
        List<Position> goals=new ArrayList<>();
        for(int y=0;y<lines.size();y++) {
            if(lines.get(y).length()!=grid.width())throw new IOException("Linhas com tamanhos diferentes");
            for(int x=0;x<grid.width();x++) {
                Position p=new Position(x,y);
                switch(lines.get(y).charAt(x)) {
                    case '#'->grid.setWall(p,true);
                    case 'S'-> {
                        if(start!=null)throw new IOException("Mais de um início");
                        start=p;
                    }
                    case 'G'->goals.add(p);
                    case '.',' '-> {
                    }
                    default->throw new IOException("Caractere inválido");
                }
            }
        }
        if(start==null||goals.isEmpty())throw new IOException("Informe S e G");
        if(metadata!=null) {
            List<Position> ordered=new ArrayList<>();
            try {
                for(String token:metadata.substring(7).split(";")) {
                    String[] xy=token.split(",");
                    if(xy.length!=2)throw new IllegalArgumentException();
                    Position p=new Position(Integer.parseInt(xy[0]),Integer.parseInt(xy[1]));
                    if(!goals.contains(p)||ordered.contains(p))throw new IllegalArgumentException();
                    ordered.add(p);
                }
            }
            catch(RuntimeException ex) {
                throw new IOException("Ordem de alvos inválida",ex);
            }
            if(ordered.size()!=goals.size())throw new IOException("Ordem de alvos incompleta");
            goals=ordered;
        }
        return new Scenario(grid,start,goals);
    }
    public static Scenario load(Path file)throws IOException {
        return read(Files.newInputStream(file));
    }
    public static void save(Path file,Scenario scenario)throws IOException {
        StringBuilder text=new StringBuilder();
        for(int y=0;y<scenario.grid.height();y++) {
            for(int x=0;x<scenario.grid.width();x++) {
                Position p=new Position(x,y);
                text.append(p.equals(scenario.start)?'S':scenario.goals.contains(p)?'G':scenario.grid.walkable(p)?'.':'#');
            }
            text.append('\n');
        }
        if(scenario.goals.size()>1) {
            text.append("@goals ");
            for(int i=0;i<scenario.goals.size();i++) {
                if(i>0)text.append(';');
                Position p=scenario.goals.get(i);
                text.append(p.x()).append(',').append(p.y());
            }
            text.append('\n');
        }
        Files.writeString(file,text);
    }
}
