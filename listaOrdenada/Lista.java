package listaOrdenada;
import java.util.Stack;

public class Lista {


   public boolean balanceamento(String s){

    Stack<Character> pilha = new Stack<>();

    for(char c: s.toCharArray()){
        if(c == '(' || c == '{' || c == '[') {
            pilha.push(c);
        }
        else{
            if(pilha.isEmpty()) return false;

            char topo = pilha.pop();

            if(c == ')' && topo != '(') return false;
            if(c == '}' && topo != '{') return false;
            if(c == ']' && topo != '[') return false;
        }
    
    }

    return pilha.isEmpty();

   }
}
