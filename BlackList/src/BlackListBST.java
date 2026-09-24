package BlackList.src;

public class BlackListBST {
    private Node root;

    public BlackListBST() {
        root = null;
    }

    public void insert(long ip){
        root = insertRecursive(root, ip);
    }

    private Node insertRecursive(Node atual, long ip){
        if(atual == null){
            return new Node(ip);
        }
        if (ip < atual.ip){
            atual.esquerdo = insertRecursive(atual.esquerdo, ip);
        }
        else if (ip > atual.ip){
            atual.direito = insertRecursive(atual.direito, ip);
        }
        else{
            atual.contadorTentativas++;
        }

        return atual;
    }

    public void search(long ip){
        Node atual = root;

        while (atual != null){
            if (ip == atual.ip){
                System.out.println("Encontrado: " + ip);

                System.out.println("Tentativas: " + atual.contadorTentativas);
                return;
            }
            if (ip < atual.ip){
                atual = atual.esquerdo;
            }
            else{
                atual = atual.direito;
            }
        }

        System.out.println("IP nao encontrado na blacklist!" );
    }

    public void relatorioOrdenado(){
        System.out.println("\n===== BLACKLIST =====");

        if (root == null) {
            System.out.println("Blacklist vazia.");
            return;
        }

        emOrdem(root);
    }

    private void emOrdem(Node atual) {

        if (atual != null) {

            emOrdem(atual.esquerdo);

            System.out.println(
                    "IP: " + atual.ip +
                            " | Tentativas: " + atual.contadorTentativas
            );

            emOrdem(atual.direito);
        }
    }

    public void remover(long ip) {

        root = removerRecursivo(root, ip);
    }

    private Node removerRecursivo(Node atual, long ip) {

        if (atual == null) {
            return null;
        }

        // Procurar na esquerda
        if (ip < atual.ip) {

            atual.esquerdo =
                    removerRecursivo(atual.esquerdo, ip);

        }

        // Procurar na direita
        else if (ip > atual.ip) {

            atual.direito =
                    removerRecursivo(atual.direito, ip);

        }

        // Encontramos o IP
        else {

            // Caso 1: não possui filhos
            if (atual.esquerdo == null &&
                    atual.direito == null) {

                return null;
            }

            // Caso 2: possui apenas filho direito
            if (atual.esquerdo == null) {
                return atual.direito;
            }

            // Caso 3: possui apenas filho esquerdo
            if (atual.direito == null) {
                return atual.esquerdo;
            }

            // Caso 4: possui dois filhos
            Node sucessor = menorNo(atual.direito);

            atual.ip = sucessor.ip;
            atual.contadorTentativas =
                    sucessor.contadorTentativas;

            atual.direito =
                    removerRecursivo(atual.direito, sucessor.ip);
        }

        return atual;
    }

    private Node menorNo(Node atual) {
        while(atual.esquerdo != null) {
            atual = atual.esquerdo;
        }
        return atual;
    }
}
