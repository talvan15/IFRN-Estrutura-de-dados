package fila;

public class Queue {
    private int frente, fim, capacidade;
    private int queue[];

    public Queue(int tamanho) {
        frente = fim = 0;
        capacidade = tamanho;
        queue = new int[capacidade];
    }

    public boolean isEmpty(){
        if (frente == fim){
            return true;
        }
        else{
            return false;
        }
    }

    public void push(int valor){
        if(fim == capacidade){
            System.out.println("A fila está cheia");
        }
        else{
            queue[fim] = valor;
            fim++;
        }
    }

    public int pop(){
        if (isEmpty()) {
            System.out.println("Fila vazia");
        }

        int valor = queue[frente];
        frente++;
        capacidade--;
        return  valor;
    }

    public int peek(){
        if (isEmpty()) {
            System.out.println("Fila vazia");
            return -1;
        }
        else{
            return queue[frente];
        }
    }

}
