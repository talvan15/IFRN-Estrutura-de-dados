package Tree.src;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Tree.src.BinaryTree tree = new Tree.src.BinaryTree();

        tree.insert(10);
        tree.insert(5);
        tree.insert(15);
        tree.insert(2);
        tree.insert(7);
        tree.insert(12);
        tree.insert(20);

        System.out.print("In-Order: ");
        tree.inOrder();

        System.out.print("Pre-Order: ");
        tree.preOrder();

        System.out.print("Post-Order: ");
        tree.postOrder();

        System.out.print("altura: ");
        tree.altura();


        tree.contarFolhas();
        System.out.println();
        tree.contarNos();
        System.out.println();
        tree.delete(10);

    }
}