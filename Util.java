import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {

    public static int[][] deepCopy(int[][] arr) {
        int[][] copy = new int[arr.length][arr[0].length];
        for (int i = 0; i < arr.length; i++)
            copy[i] = arr[i].clone();
        return copy;
    }

    public static List<Integer> toFlatList(int[][] arr) {
        List<Integer> copy = new ArrayList<>();
        for (int[] a: arr)
            for (int e: a)
                copy.add(e);
        return copy;
    }

    /**
     * Prints out the board for debugging purposes.
     */
    public static void printBoard(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                char c;
                switch (board[i][j]) {
                    case Game.BLACK: c = '●';
                        break;
                    case Game.WHITE: c = '○';
                        break;
                    case Game.EMPTY: c = '✕';
                        break;
                    default: c = '－';
                        break;
                }
                System.out.print(c);
            }
            System.out.println();
        }
    }
}
