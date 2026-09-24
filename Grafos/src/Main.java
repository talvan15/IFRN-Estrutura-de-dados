//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Grafo grafo = new Grafo();

        Vertice v1 = new Vertice("A");
        Vertice v2 = new Vertice("B");
        Vertice v3 = new Vertice("C");
        Vertice v4 = new Vertice("D");

        grafo.adicionarVertice(v1);
        grafo.adicionarVertice(v2);
        grafo.adicionarVertice(v3);
        grafo.adicionarVertice(v4);

        grafo.adicionarAresta(v1, v2);
        grafo.adicionarAresta(v2, v3);
        grafo.adicionarAresta(v3, v1);
        grafo.adicionarAresta(v4, v1);

        System.out.println("Vertices do grafo:");
        for (Vertice vertice : grafo.getVertices()) {
            System.out.println(vertice.getValor());
        }

        System.out.println("\nArestas do grafo:");
        for (Aresta aresta : grafo.getArestas()) {
            System.out.println(aresta.getOrigem().getValor() + " -> " + aresta.getDestino().getValor());
        }
    }
}