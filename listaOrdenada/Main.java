package listaOrdenada;

public class Main {
    public static void main(String[] args) {
        Lista lista = new Lista();

        String expressao1 = "([)]";
        String expressao2 = "{[(])}";
        String expressao3 = "{{[[(())]]}}";

        System.out.println("Expressão: " + expressao1 + " está balanceada? " + lista.balanceamento(expressao1));
        System.out.println("Expressão: " + expressao2 + " está balanceada? " + lista.balanceamento(expressao2));
        System.out.println("Expressão: " + expressao3 + " está balanceada? " + lista.balanceamento(expressao3));

    }
    
}
