import java.util.Scanner;

public class MancalaBoard {

    private int[] board; // 0-5 on p1 side, 6-11 on p2
    private int p1, p2;
    private boolean p1Turn;

    public MancalaBoard() {
        board = new int[12];
        // set all values to 4
        for (int i = 0; i < board.length; i++) {
            board[i] = 4;
        }

        // each player has 0 stones
        p1 = 0;
        p2 = 0;

        p1Turn = true;
    }

    public MancalaBoard(int[] board, int p1, int p2, boolean p1Turn) {
        this.board = board;
        this.p1 = p1;
        this.p2 = p2;
        this.p1Turn = p1Turn;
    }

    public int[] getBoard() {
        return board;
    }

    public int getP1() {
        return p1;
    }

    public int getP2() {
        return p2;
    }

    public boolean getP1Turn() {
        return p1Turn;
    }

    /**
     * Checks if game is over. Returns true if is, false if not.
     * If game is over, give correct player the remaining pieces.
     */
    boolean isGameOver() {
        int index = 0;
        while (index < 6 && board[index] == 0) {
            index++;
        }
        if (index == 6) {
            for (int i = 6; i < board.length; i++) {
                p2 += board[i];
            }
            return true;
        }
        // check other side
        index = 6;
        while (index < board.length && board[index] == 0) {
            index++;
        }
        if (index == board.length) {
            for (int i = 0; i < 6; i++) {
                p1 += board[i];
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the differential between p1 and p2.
     * @return differential between p1 and p2
     */
    int getWinner() {
        return p1 - p2;
    }

    void printBoard() {
        for (int i = 11; i > 5; i--) {
            System.out.print(board[i] + "\t");
        }
        System.out.println();
        for (int i = 0; i < 6; i++) {
            System.out.print(board[i] + "\t");
        }
        System.out.println();
        System.out.println("P1: " + p1);
        System.out.println("P2: " + p2);
        System.out.println("P1 move: " + p1Turn);
    }

}
