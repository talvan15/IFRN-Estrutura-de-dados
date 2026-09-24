package Tree.src;

public class BinaryTree {

    private Node root;

    // 4) Inserir
    public void insert(int value) {
        root = insertRecursive(root, value);
    }

    private Node insertRecursive(Node actual, int value) {

        if (actual == null) {
            return new Node(value);
        }

        if (value < actual.value) {
            actual.left = insertRecursive(actual.left, value);

        } else if (value > actual.value) {
            actual.right = insertRecursive(actual.right, value);
        }

        return actual;
    }

    public void inOrder() {
        inOrderRecursive(root);
        System.out.println();
    }

    private void inOrderRecursive(Node actual) {

        if (actual != null) {
            inOrderRecursive(actual.left);
            System.out.print(actual.value + " ");
            inOrderRecursive(actual.right);
        }
    }

    public void preOrder() {
        preOrderRecursive(root);
        System.out.println();
    }

    private void preOrderRecursive(Node actual) {

        if (actual != null) {
            System.out.print(actual.value + " ");
            preOrderRecursive(actual.left);
            preOrderRecursive(actual.right);
        }
    }

    public void postOrder() {
        postOrderRecursive(root);
        System.out.println();
    }

    private void postOrderRecursive(Node actual) {

        if (actual != null) {
            postOrderRecursive(actual.left);
            postOrderRecursive(actual.right);
            System.out.print(actual.value + " ");
        }
    }

    // 1) Altura
    public void altura() {
        alturaRecursive(root);
    }

    private int alturaRecursive(Node no) {

        if (no == null) {
            return 0;
        }

        int alturaEsquerda = alturaRecursive(no.left);
        int alturaDireita = alturaRecursive(no.right);

        return Math.max(alturaEsquerda, alturaDireita) + 1;
    }

    // 2) Número de nós
    public void contarNos() {
        contarNosRecursive(root);
    }

    private int contarNosRecursive(Node no) {

        if (no == null) {
            return 0;
        }

        return 1
                + contarNosRecursive(no.left)
                + contarNosRecursive(no.right);
    }

    // 3) Número de folhas
    public void contarFolhas() {
        contarFolhasRecursive(root);
    }

    private int contarFolhasRecursive(Node no) {

        if (no == null) {
            return 0;
        }

        if (no.left == null && no.right == null) {
            return 1;
        }

        return contarFolhasRecursive(no.left)
                + contarFolhasRecursive(no.right);
    }

    // 5) Deletar nó
    public void delete(int value) {
        root = deleteRecursive(root, value);
    }

    private Node deleteRecursive(Node actual, int value) {

        if (actual == null) {
            return null;
        }

        if (value < actual.value) {

            actual.left = deleteRecursive(actual.left, value);

        } else if (value > actual.value) {

            actual.right = deleteRecursive(actual.right, value);

        } else {

            if (actual.left == null && actual.right == null) {
                return null;
            }

            if (actual.left == null) {
                return actual.right;
            }

            if (actual.right == null) {
                return actual.left;
            }

            int menorValor = encontrarMenor(actual.right);

            actual.value = menorValor;

            actual.right = deleteRecursive(
                    actual.right,
                    menorValor
            );
        }

        return actual;
    }

    // Encontrar menor valor
    private int encontrarMenor(Node no) {

        while (no.left != null) {
            no = no.left;
        }

        return no.value;
    }
}