package BlackList.src;

public class Node {
    long ip;
    int contadorTentativas;

    Node esquerdo;
    Node direito;

    public  Node(long ip) {
        this.ip = ip;
        this.contadorTentativas = 1;
        this.esquerdo = null;
        this.direito = null;
    }
}
