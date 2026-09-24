import java.util.ArrayList;
import java.util.List;

public class Grafo {
    private List<Vertice> vertices;
    private List<Aresta> aresta;


    public Grafo() {
        vertices = new ArrayList<>();
        aresta = new ArrayList<>();
    }

    public void adicionarVertice(Vertice vertice) {
        vertices.add(vertice);
    }

    public void adicionarAresta(Vertice origem, Vertice destino) {
        Aresta aresta = new Aresta(origem, destino);
        this.aresta.add(aresta);

        origem.adicionarVizinho(destino);
        destino.adicionarVizinho(origem);
    }

    public List<Vertice> getVertices() {
        return vertices;
    }

    public List<Aresta> getArestas() {
        return aresta;
    }
}
