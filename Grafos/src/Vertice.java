import java.util.ArrayList;
import java.util.List;

public class Vertice {
    private String valor;
    private List<Vertice> vizinhos;

    public Vertice(String valor) {
        this.valor = valor;
        this.vizinhos = new ArrayList<>();
    }

    public String getValor() {
        return valor;
    }

    public List<Vertice> getVizinhos() {
        return vizinhos;
    }

    public void adicionarVizinho(Vertice vizinho) {
        vizinhos.add(vizinho);
    }
}
