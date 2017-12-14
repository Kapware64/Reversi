/*
 * Game.java
 *
 * Version:
 *    $Id$
 *
 * Revisions:
 *    &Log$
 *
 */

import java.util.ArrayList;
import java.util.Stack;

/**
 * This class specifies the game state for a 2-dimensional board game.
 *
 * @author Francis Yuan
 */

public class Game {
    public static final int OFFBOARD = -1;
    public static final int BLACK = 0;
    public static final int WHITE = 1;
    public static final int EMPTY = 2;
    final static int WIDTH = 10;
    final static int HEIGHT = 10;
    final static int BOARD_SIZE = 8;
    public int board[][] = new int[WIDTH][HEIGHT];
    public Stack<int[][]> boardHistory = new Stack<>();
    public int[] recentMove;

    /**
     * Default constructor
     */
    public Game() {
    }

    /**
     * Creates a copy of the game
     *
     * @param another The game to be copied
     */
    public Game(Game another) {
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                this.board[i][j] = another.board[i][j];
            }
        }
    }

    /**
     * Initialize Reversi board
     */
    public void init() {
        // Initialize off-board squares
        for (int i = 0; i < WIDTH; i++) {
            board[i][0] = OFFBOARD;
            board[i][WIDTH - 1] = OFFBOARD;
            board[0][i] = OFFBOARD;
            board[HEIGHT - 1][i] = OFFBOARD;
        }

        // Initialize game board to be empty except for initial setup
        for (int i = 1; i < HEIGHT - 1; i++)
            for (int j = 1; j < WIDTH - 1; j++)
                board[i][j] = EMPTY;

        board[HEIGHT / 2 - 1][WIDTH / 2 - 1] = WHITE;
        board[HEIGHT / 2][WIDTH / 2 - 1] = BLACK;
        board[HEIGHT / 2 - 1][WIDTH / 2] = BLACK;
        board[HEIGHT / 2][WIDTH / 2] = WHITE;
    }

    /**
     * Decide if the move is legal
     *
     * @param r     row in the game matrix
     * @param c     column in the game matrix
     * @param color color of the player - Black or White
     * @param flip  true if the player wants to flip the discs
     * @return true if the move is legal, else false
     */
    public boolean legalMove(int r, int c, int color, boolean flip) {
        // Initialize boolean legal as false
        boolean legal = false;

        // If the cell is empty, begin the search
        // If the cell is not empty there is no need to check anything
        // so the algorithm returns boolean legal as is
        if (board[r][c] == EMPTY) {
            // Initialize variables
            int posX;
            int posY;
            boolean found;
            int current;

            // Searches in each direction
            // x and y describe a given direction in 9 directions
            // 0, 0 is redundant and will break in the first check
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    // Variables to keep track of where the algorithm is and
                    // whether it has found a valid move
                    posX = c + x;
                    posY = r + y;
                    found = false;
                    current = board[posY][posX];

                    // Check the first cell in the direction specified by x and y
                    // If the cell is empty, out of bounds or contains the same color
                    // skip the rest of the algorithm to begin checking another direction
                    if (current == OFFBOARD || current == EMPTY || current == color) {
                        continue;
                    }

                    // Otherwise, check along that direction
                    while (!found) {
                        posX += x;
                        posY += y;
                        current = board[posY][posX];

                        // If the algorithm finds another piece of the same color along a direction
                        // end the loop to check a new direction, and set legal to true
                        if (current == color) {
                            found = true;
                            legal = true;

                            // If flip is true, reverse the directions and start flipping until
                            // the algorithm reaches the original location
                            if (flip) {
                                posX -= x;
                                posY -= y;
                                current = board[posY][posX];

                                while (current != EMPTY) {
                                    board[posY][posX] = color;
                                    posX -= x;
                                    posY -= y;
                                    current = board[posY][posX];
                                }
                            }
                        }
                        // If the algorithm reaches an out of bounds area or an empty space
                        // end the loop to check a new direction, but do not set legal to true yet
                        else if (current == OFFBOARD || current == EMPTY) {
                            found = true;
                        }
                    }
                }
            }
        }
        return legal;
    }

    /**
     * List of all legal moves that can be played by the player
     * @param player
     * @return List of legal moves
     */
    public ArrayList<int[]> getLegalMoves(int player) {
        ArrayList<int[]> legalMoves = new ArrayList<>();
        for (int i = 1; i <= BOARD_SIZE; i++)
            for (int j = 1; j <= BOARD_SIZE; j++)
                if (legalMove(i, j, player, false))
                    legalMoves.add(new int[]{i, j});
        return legalMoves;
    }

    public boolean hasLegalMoves(int player) {
        return !getLegalMoves(player).isEmpty();
    }

    /**
     * Place disk and process flips after placing a disk
     * @param row row index
     * @param col column index
     * @param color color of disk
     */
    public void placeDisk(int row, int col, int color) {
        board[row][col] = color;
        recentMove = new int[]{row, col};
        int opponent = toggleTurn(color);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                int x = row + dx;
                int y = col + dy;
                while (board[x][y] == opponent) {
                    x += dx;
                    y += dy;
                }
                if (board[x][y] == color) {
                    x -= dx;
                    y -= dy;
                    while (board[x][y] == opponent){
                        board[x][y] = color;
                        x -= dx;
                        y -= dy;
                    }
                }
            }
        }
    }

    /**
     * A modification of legalMove() that uses a simple class to hold data
     * about the potential move
     *
     * @param r     Row in the game matrix
     * @param c     Column in the game matrix
     * @param color Color of the player - Black or White
     * @param flip  True if the player wants to flip the discs
     * @return A move object that also indicates whether or not the move is legal
     * @param     point         A table of Multipoints to determine the numeric value of a move
     */
    public Move pointMove(int r, int c, int color, boolean flip, int[][] point) {
        // Initialize a default Move object
        Move newMove = new Move();

        if (board[r][c] == EMPTY) {
            int posX;
            int posY;
            boolean found;
            int current;
            int sum;

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    posX = c + x;
                    posY = r + y;
                    found = false;
                    current = board[posY][posX];
                    sum = 0;

                    if (current == OFFBOARD || current == EMPTY || current == color) {
                        continue;
                    } else {
                        // First piece is an enemy so add to the point count
                        sum += point[posY][posX];
                    }

                    while (!found) {
                        posX += x;
                        posY += y;
                        current = board[posY][posX];

                        if (current == color) {
                            found = true;
                            newMove.legal = true;
                            newMove.x = c;
                            newMove.y = r;
                            newMove.points += point[c][r];

                            if (flip) {
                                posX -= x;
                                posY -= y;
                                current = board[posY][posX];

                                while (current != EMPTY) {
                                    board[posY][posX] = color;
                                    posX -= x;
                                    posY -= y;
                                    current = board[posY][posX];
                                }
                            }
                        } else if (current == OFFBOARD || current == EMPTY) {
                            // The pieces in this direction won't be flipped so reset sum to 0
                            sum = 0;
                            found = true;
                        } else {
                            // Piece is an enemy so add to the point count
                            sum += point[posY][posX];
                        }
                    }

                    // Done checking this direction so add the sum to the Move object point co
                    newMove.points += sum;
                }
            }
        }
        return newMove;
    }


    /**
     * Check if game reached the end
     * @return No more moves to make
     */
    public boolean endReached() {
        return !hasLegalMoves(BLACK) && !hasLegalMoves(WHITE);
    }


    public static int toggleTurn(int color) {
        return color == BLACK ? WHITE : BLACK;
    }

    /**
     * Count number of disks for each player
     * @return int array of disk counts
     */
    public int[] getDiskCount() {
        int[] diskCount = new int[Othello.PLAYER_SIZE];
        for (int i = 1; i <= BOARD_SIZE; i++)
            for (int j = 1; j <= BOARD_SIZE; j++)
                if (board[i][j] == BLACK || board[i][j] == WHITE)
                    diskCount[board[i][j]]++;
        return diskCount;
    }

    /**
     * Which player won
     * @return the winner player, EMPTY if game was a tie
     */
    public int getWinner() {
        int[] diskCount = getDiskCount();
        if (diskCount[WHITE] > diskCount[BLACK])
            return WHITE;
        if (diskCount[BLACK] > diskCount[WHITE])
            return BLACK;
        return EMPTY;
    }
}
