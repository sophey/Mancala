import java.util.Scanner;

/**
 * Created by Sophey on 11/26/16.
 */
public class Mancala {

    /**
     * Moves starting with given index.
     * @param index index of the starting piece
     */
    static MancalaBoard move(int index, MancalaBoard m) {
        boolean p1Turn = m.getP1Turn();
        int[] board = new int[12];
        System.arraycopy(m.getBoard(), 0, board, 0, 12);
        int p1 = m.getP1();
        int p2 = m.getP2();
        if (p1Turn && index > 5) {
            return m;
        } else if (!p1Turn && index < 6){
            return m;
        }
        if (board[index] == 0) {
            return m;
        }
        int space = (index + 1) % 12;
        boolean lastStore = false;
        // while there are still seeds, move
        while (board[index] != 0) {
            board[index]--;
            if (!lastStore && p1Turn && space == 6) {
                p1++;
                lastStore = true;
            } else if (!lastStore && !p1Turn && space == 0) {
                p2++;
                lastStore = true;
            } else {
                board[space]++;
                space = (space + 1) % 12;
                lastStore = false;
            }
        }
        // checks if dropped in your own store on last move, do not go past
        if (lastStore) {
            return new MancalaBoard(board, p1, p2, p1Turn);
        }
        // set to last space
        space = Math.floorMod(space - 1, 12);
        // checks if dropped in an empty hole on your side, and there are pieces across
        if (p1Turn && space < 6 && board[space] == 1 && board[11 - space] > 0) {
            p1 += board[11 - space] + 1;
            board[11 - space] = 0;
            board[space] = 0;
        } else if (!p1Turn && space > 5 && board[space] == 1 && board[11 - space] > 0) {
            p2 += board[11 - space] + 1;
            board[11 - space] = 0;
            board[space] = 0;
        }
        // set to next player's move
        p1Turn = !p1Turn;
        return new MancalaBoard(board, p1, p2, p1Turn);
    }

    /**
     * MiniMax with p1 as max
     * @param m MancalaBoard
     * @return minimax value
     */
    static int miniMax(MancalaBoard m, int depth) {
        if (depth == 0 || m.isGameOver()) {
            return m.getWinner();
        }
        if (m.getP1Turn()) {
            int bestValue = Integer.MIN_VALUE;
            for (int i = 0; i < 6; i++) {
                MancalaBoard newBoard = move(i, m);
                if (!newBoard.equals(m)) {
                    int val = miniMax(newBoard, depth - 1);
                    bestValue = Math.max(val, bestValue);
                }
            }
            return bestValue;
        } else {
            int bestValue = Integer.MAX_VALUE;
            for (int i = 6; i < 12; i++) {
                MancalaBoard newBoard = move(i, m);
                if (!newBoard.equals(m)) {
                    int val = miniMax(newBoard, depth - 1);
                    bestValue = Math.min(val, bestValue);
                }
            }
            return bestValue;
        }
    }

    static int alphaBeta(MancalaBoard m, int depth, int alpha, int beta) {
        if (depth == 0 || m.isGameOver()) {
            return m.getWinner();
        }
        if (m.getP1Turn()) {
            int bestValue = Integer.MIN_VALUE;
            for (int i = 0; i < 6; i++) {
                MancalaBoard newBoard = move(i, m);
                if (!newBoard.equals(m)) {
                    int val = alphaBeta(newBoard, depth - 1, alpha, beta);
                    bestValue = Math.max(val, bestValue);
                    alpha = Math.max(alpha, val);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return bestValue;
        } else {
            int bestValue = Integer.MAX_VALUE;
            for (int i = 6; i < 12; i++) {
                MancalaBoard newBoard = move(i, m);
                if (!newBoard.equals(m)) {
                    int val = alphaBeta(newBoard, depth - 1, alpha, beta);
                    bestValue = Math.min(val, bestValue);
                    beta = Math.min(beta, val);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return bestValue;
        }
    }

    /**
     * Gets ideal move using minimax
     * @param m MancalaBoard
     * @return best move value
     */
    static int chooseMove(MancalaBoard m) {
        // p1's turn
        if (m.getP1Turn()) {
            int bestValue = Integer.MIN_VALUE;
            int max = 0;
            for (int i = 0; i < 6; i++) {
                MancalaBoard newBoard = move(i, m);
                if (!newBoard.equals(m)) {
                    int val = alphaBeta(newBoard, 10, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    if (val > bestValue) {
                        max = i;
                        bestValue = val;
                    }
                }
            }
            return max;
        } else {
            int bestValue = Integer.MAX_VALUE;
            int min = 0;
            for (int i = 6; i < 12; i++) {
                MancalaBoard newBoard = move(i, m);
                if (!newBoard.equals(m)) {
                    int val = alphaBeta(newBoard, 10, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    if (val < bestValue) {
                        min = i;
                        bestValue = val;
                    }
                }
            }
            return min;
        }
    }

    public static void main(String[] args) {
        MancalaBoard game = new MancalaBoard();
        Scanner s = new Scanner(System.in);
        System.out.print("Do you want to be player 1 or 2? ");
        int player = s.nextInt();
        while(!game.isGameOver()) {
            game.printBoard();
            if ((player == 1 && game.getP1Turn()) || (player == 2 && !game.getP1Turn())) {
                System.out.println("Your move: ");
                int move = s.nextInt();
                MancalaBoard next = Mancala.move(move, game);
                if (game.equals(next)) {
                    System.out.println("Cannot make that move, choose another spot.");
                } else {
                    game = next;
                }
            } else {
                int move = Mancala.chooseMove(game);
                System.out.println(move);
                game = move(move, game);
            }
        }
        game.printBoard();
    }

}
