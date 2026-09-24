package pilha;

public class Main {
    public static void main(String[] args) {
        Pilha minhaPilha = new Pilha(5);

        System.out.println("Pilha vazia? " + minhaPilha.isEmpty());

        minhaPilha.empilhar(10);
        minhaPilha.empilhar(20);
        minhaPilha.empilhar(30);

        System.out.println("Elemento no topo: " + minhaPilha.elementoTopo());

        System.out.println("Desempilhado: " + minhaPilha.desempilhar());
        System.out.println("Elemento no topo após desempilhar: " + minhaPilha.elementoTopo());

        System.out.println("Pilha vazia: " + minhaPilha.isEmpty());
    }
}
