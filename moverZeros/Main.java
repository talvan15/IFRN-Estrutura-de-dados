package moverZeros;


public class Main {
    public static void main(String[] args) {
        
        int array[] = {0, 1, 0, 3, 12};

        MoveZeros mover = new MoveZeros();
        mover.moveZeroes(array);

        mover.printArray(array);
    }
}
