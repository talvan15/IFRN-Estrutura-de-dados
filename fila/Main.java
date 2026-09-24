package fila;

public class Main {
    public static void main(String[] args) {
        Queue fila = new Queue(10);

        fila.push(5);
        fila.push(10);
        fila.push(15);

        fila.pop();
        System.out.println(fila.isEmpty());

        System.out.println(fila.peek());
    }
}
