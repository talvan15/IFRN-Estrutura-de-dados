package listaEncadeada;
class ListaEcadeada{
    No cabeca;

    public void inserirInicio(int valor){
        No novo = new No(valor); //Atribuição do valor ao Nó novo;
        novo.proximo = cabeca; // O termo novo aponta para o proximo nó, que era a cabeça.
        cabeca = novo; // o nó cabeça vira o elemento novo que foi inserido.
    }

    public void inserirFim(int valor){
        No novo = new No(valor); //atribuição do valor ao nó novo;

        if (cabeca == null) cabeca = novo; //verifica se a lista esta vazia e insere, se estiver;
        else{
            No atual = cabeca; // o primeiro nó será a cabeça.
            while (atual.proximo != null){ // percorre a lista.
                atual = atual.proximo; //Nó atual vai sendo atualizado para o pŕoximo nó
            }
            atual.proximo = novo; // quando o atual.próximo for NULL, o atual recebe o valor.
        }
    }

    public void exibir(){
        No atual = cabeca;
        while(atual != null){
            System.out.print(atual.valor + " ");
            atual = atual.proximo;
        }
        System.out.println("NULL");
    }

    //(b) ObterProximo - Recebe como argumento um nó e retorna o próximo
    public void ObterProximo(int no){
        No atual = cabeca;
        if (cabeca == null){
            System.out.println("Lista vazia!");
        }
        else {
            while (atual != null) {
                if (atual.valor == no) {
                    atual = atual.proximo;
                    System.out.println("Próximo valor: " + atual.valor);
                }
                else{
                    atual  = atual.proximo;
                }
            }
        }
    }

    //(c) ObterValor - Recebe como argumento um nó e retorna os valores armazenados dentro
    //dele
    public int ObterValor(int no){
        No atual = cabeca;
        int valor = 0;

        if (cabeca == null) System.out.println("Lista vazia!");
        else{
            while (atual != null){
                if (atual.valor == no){
                    valor = atual.valor;
                    break;
                }
                atual = atual.proximo;
            }
        }
        return valor;
    }
    //(d) AlterarNo - Recebe como argumento um nó e dois interios para alterar as informações
    //do nó referenciado
    public void AlterarNo(int no, int novoValor){
        No atual = cabeca;
        if (cabeca == null) System.out.println("Lista vazia!");

        while (atual != null){
            if (atual.valor == no) atual.valor = novoValor;
            atual = atual.proximo;
        }
    }

    //(e) Tamanho - Retorna o tamanho da lista
    public void tamanho(){
        int count = 0;
        if (cabeca == null){
            System.out.println("Lista vazia!");
        }
        else{
            No atual = cabeca;
            while (atual != null){
                atual = atual.proximo;
                count++;
            }
            System.out.println("Tamanho da lista: " + count);
        }
    }

    //(f) Existe - Retorna se um n ́o existe na lista
    public boolean existe(int no){
        No atual = cabeca;
        boolean isHere = false;
        while(atual != null){
            if (atual.valor == no){
                isHere = true;
                break;
            }
            atual = atual.proximo;
        }

        return isHere;
    }

    public int retornarValorNo(No no){
        return no.valor;
    }
}