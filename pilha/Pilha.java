package pilha;

public class Pilha {

    private int topo, capacidade;
    private int pilha[];

    public Pilha(int tamanho){
        topo = 0;
        capacidade = tamanho;
        pilha = new int[tamanho];
    }
    public boolean isEmpty(){
        if(topo == 0){
            return true;
        }
        return false;
    }

    public void empilhar(int valor){
        if(topo == capacidade){
            System.out.println("Pilha cheia!");
        } else {
            pilha[topo] = valor;
            topo++;
        }
    }

    public int desempilhar(){
        if(isEmpty()){
            return -1;
        }
        topo--;
        return pilha[topo];
    }

    public int elementoTopo(){
        if(isEmpty()){
            return -1;
        }
        return pilha[topo - 1];
    }
}
