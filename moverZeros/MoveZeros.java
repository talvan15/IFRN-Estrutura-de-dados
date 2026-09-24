package moverZeros;

public class MoveZeros {

    public void moveZeroes(int[] nums) {
        
        int posicaoNaoZero = 0;
        
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                nums[posicaoNaoZero] = nums[i];
                posicaoNaoZero++;
            }
        }
        
        for (int i = posicaoNaoZero; i < nums.length; i++) {
            nums[i] = 0;
        }
            
    }

    public void printArray(int[] nums) {
        for (int num : nums) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
    
}
