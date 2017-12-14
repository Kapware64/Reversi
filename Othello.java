/*
 * Othello.java
 *
 * Version:
 *    $Id$
 *
 * Revisions:
 *    &Log$
 *
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

/**
 * This program is the driver for the game of Othello.<br>
 * <p>
 * Run the program as one of the following:<br>
 * java Othello          (GUI with a default delay time of 1 second)<br>
 * java Othello delay    (GUI with a delay of (delay) milliseconds)<br>
 * java Othello 0        (GUI with human (Black) vs. machine (White))<br>
 * java Othello -delay   (No GUI - run program (delay) times)<br>
 *
 * @author Roxanne Canosa
 */

public class Othello extends JPanel {
    final static int PLAYER_SIZE = 2;

    private Game game = new Game();     // Game state
    private Strategy strategy = new Strategy();
    private Timer timer;
    private static int delay;
    private int turn = Game.BLACK;

    Player[] players = new Player[PLAYER_SIZE];
    private boolean[] playerDone = new boolean[PLAYER_SIZE];

    /**
     * This constructor sets up the initial game configuration,
     * and starts the timer with a default delay of 1 second.
     */
    public Othello() {
        this(0);
    }

    /**
     * This constructor sets up the initial game configuration,
     * and starts the timer with a user specified delay.
     *
     * @param delay number of milliseconds between player moves
     */
    public Othello(int delay) {
        players[Game.BLACK] = new Player(strategy, AIType.NaiveMonteCarlo, Game.BLACK);          // The players
        players[Game.WHITE] = new Player(strategy, AIType.NaiveMonteCarlo, Game.WHITE);

        // Initialize the game state
        initGame(game);
        GameStats stats = new GameStats();

        // Run the game with GUI - computer vs. computer using a timer
        if (delay > 0) {
            setBackground(Color.GREEN);
            timer = new javax.swing.Timer(delay, e -> {
                playerMove();
                repaint();
            });

            // Create the Start and Stop buttons
            final String _start = "Start";
            final String _stop = "Stop";
            JButton start = new JButton(_start);
            start.setBounds(10, 20, 80, 25);
            add(start);
            start.addActionListener(evt -> {
                if (start.getText().equals(_start)) {
                    timer.start();
                    start.setText(_stop);
                }
                else if (start.getText().equals(_stop)) {
                    timer.stop();
                    start.setText(_start);
                }
            });

            final String _reset = "Reset";
            JButton reset = new JButton(_reset);
            reset.setBounds(100, 20, 80, 25);
            add(reset);
            reset.addActionListener(e -> {
                initGame(game);
                timer.stop();
                start.setText(_start);
                repaint();
            });
        }

        // Run the game with GUI - human vs. computer. 
        // The human player always plays with the black discs.
        if (delay == 0) {
            setBackground(Color.GREEN);

            final String _black = "Black";
            final String _white = "White";
            String[] colors = new String[]{_black, _white};
            JComboBox colorList = new JComboBox(colors);
            colorList.setBounds(10, 20, 80, 25);
            add(colorList);

            final String _start = "Start";
            final String _reset = "Reset";
            JButton start = new JButton(_start);
            start.setBounds(100, 20, 80, 25);
            add(start);

            // Undo button
            String _undo = "Undo";
            JButton undo = new JButton(_undo);
            undo.setBounds(190, 20, 80, 25);
            undo.setEnabled(false);
            add(undo);
            undo.addActionListener(e -> {
                if (game.boardHistory.isEmpty())
                    System.out.println("No more undo history.");
                else{
                    game.board = game.boardHistory.pop();
                    repaint();
                }
            });
            MouseAdapter mouseAdapter = new MouseAdapter() {
                public void mousePressed(MouseEvent evt) {
                    // Find out which square was clicked
                    int x = evt.getX();
                    int y = evt.getY();
                    int screenWidth = getWidth();
                    int screenHeight = getHeight();
                    int column = x * Game.BOARD_SIZE / screenWidth + 1;
                    int row = y * Game.BOARD_SIZE / screenHeight + 1;
                    int[][] boardCopy = Util.deepCopy(game.board);
                    game.boardHistory.push(boardCopy);
                    if (!game.legalMove(row, column, turn, true)) {
                        System.out.println("Not a legal move - try again!");
                        game.boardHistory.pop();
                    }
                    else {
                        // Update human move
                        game.board[row][column] = turn;
                        repaint();

                        // Computer plays
                        do {
                            turn = Game.toggleTurn(turn);
                            playerMove();
                            repaint();
                        } while (!game.endReached() && !game.hasLegalMoves(turn));
                    }
                }
            };
            start.addActionListener(e -> {
                if (start.getText().equals(_start)) {
                    start.setText(_reset);
                    colorList.setEnabled(false);
                    undo.setEnabled(true);
                    game.boardHistory.clear();
                    if (colorList.getSelectedItem().equals(_white)) {
                        playerMove();
                        repaint();
                    }
                    addMouseListener(mouseAdapter);
                }
                else if (start.getText().equals(_reset)) {
                    start.setText(_start);
                    colorList.setEnabled(true);
                    undo.setEnabled(false);
                    initGame(game);
                    removeMouseListener(mouseAdapter);
                    repaint();
                }
            });
        }

        // Run the game without the GUI - as many times as specified in delay.
        if (delay < 0) {

            // Start timing how long it takes to play "delay" games
            long startTime = System.currentTimeMillis();

            // Keep track of how many wins each color has
            int white_won = 0;
            int black_won = 0;
            int ties = 0;

            // Play a bunch of games!
//            MonteCarlo.stats = stats;

            for (int times = 0; times < -delay; times++) {
//                stats.addNewList();

                initGame(game);
                boolean done = false;
                for (int i = 0; i < playerDone.length; i++)
                    playerDone[i] = false;

                while (!done) {
                    playerMove();
                    done = game.endReached();
                    if (done) {
                        int[] diskCount = game.getDiskCount();
                        if (diskCount[Game.WHITE] > diskCount[Game.BLACK]) {
                            //System.out.println("White won with " + wC);
                            white_won++;
                        } else if (diskCount[Game.BLACK] > diskCount[Game.WHITE]) {
                            //System.out.println("Black won with "+ bC);
                            black_won++;
                        } else {
                            //System.out.println("Tied game");
                            ties++;
                        }
                    }
                }
            }

            long runTime = System.currentTimeMillis() - startTime;

            System.out.println("===========================");
            System.out.println("Total number of games = " + -delay);
            System.out.println("White won " + white_won + " times");
            System.out.println("Black won " + black_won + " times");
            System.out.println("Number of tied games = " + ties);
            System.out.print("Runtime for " + -delay + " games = ");
            System.out.println(runTime + " milliseconds");
            System.out.println("===========================");

            // Game stats code
//            for (int i = 0; i < -delay; i++) {
//                System.out.println("Game " + i);
//                System.out.println("Naive MT Depths");
//                for (int n : stats.naiveDepths.get(i))
//                    System.out.print(n + " ");
//                System.out.println();
//                System.out.println("Smart MT Depths");
//                for (int n : stats.smartDepths.get(i))
//                    System.out.print(n + " ");
//                System.out.println();
//                System.out.println("Naive Total Simulations");
//                for (int n : stats.naiveSims.get(i))
//                    System.out.print((n * 1.0 / MonteCarlo.SIM_TIME * 1000) + " ");
//                System.out.println();
//                System.out.println("Smart Total Simulations");
//                for (int n : stats.smartSims.get(i))
//                    System.out.print((n * 1.0 / MonteCarlo.SIM_TIME * 1000) + " ");
//                System.out.println();
//                System.out.println("Branching Factors");
//                for (int n : stats.branchingFactors.get(i))
//                    System.out.print(n + " ");
//                System.out.println();
//                System.out.println("Simulation Win Rates");
//                for (double n : stats.confidence.get(i))
//                    System.out.print(n + " ");
//                System.out.println();
//            }
        }
    }

    /**
     * Initialize the game state
     *
     * @param game the Game state
     */
    public void initGame(Game game) {

        turn = Game.BLACK;
        //System.out.println("Turn is: " + turn);

        game.init();
    }

    /**
     * A player makes a move when the Timer goes off. Black goes
     * first, and then Black and White take turns.
     */
    public void playerMove() {
        game = players[turn].strategy(game, turn);
        turn = Game.toggleTurn(turn);
    }

    /**
     * Draw the board and the current state of the game.
     *
     * @param g the graphics context of the game
     */
    public void paintComponent(Graphics g) {

        super.paintComponent(g);  // Fill panel with background color

        int width = getWidth();
        int height = getHeight();
        int xoff = width / Game.BOARD_SIZE;
        int yoff = height / Game.BOARD_SIZE;

        int bCount = 0;
        int wCount = 0;

        // Draw the lines on the board
        g.setColor(Color.BLACK);
        for (int i = 1; i <= Game.BOARD_SIZE; i++) {
            g.drawLine(i * xoff, 0, i * xoff, height);
            g.drawLine(0, i * yoff, width, i * yoff);
        }

        // Draw discs on the board and show the legal moves
        for (int i = 1; i <= Game.BOARD_SIZE; i++) {
            for (int j = 1; j <= Game.BOARD_SIZE; j++) {
                // Draw the discs
                if (game.board[i][j] == Game.BLACK) {
                    g.setColor(Color.BLACK);
                    g.fillOval((j * yoff) - yoff + 7, (i * xoff) - xoff + 7, 50, 50);
                    bCount++;
                } else if (game.board[i][j] == Game.WHITE) {
                    g.setColor(Color.WHITE);
                    g.fillOval((j * yoff) - yoff + 7, (i * xoff) - xoff + 7, 50, 50);
                    wCount++;
                }
                // Show the legal moves for the current player
                if (game.legalMove(i, j, turn, false)) {
                    g.setColor(turn == Game.BLACK ? Color.BLACK : Color.WHITE);
                    g.fillOval((j * yoff + 29) - yoff, (i * xoff + 29) - xoff, 6, 6);
                }
                if (game.recentMove != null && i == game.recentMove[0] && j == game.recentMove[1]) {
                    g.setColor(Color.RED);
                    g.fillRect((j * yoff) - yoff + 7, (i * xoff) - xoff + 7, 10, 10);
                }
            }
        }

        g.setColor(Color.RED);
        if (game.endReached()) {
            if (wCount > bCount)
                g.drawString("White won with " + wCount + " discs.", 10, 20);
            else if (bCount > wCount)
                g.drawString("Black won with " + bCount + " discs.", 10, 20);
            else g.drawString("Tied game", 10, 20);
        } else {
            if (wCount > bCount)
                g.drawString("White is winning with " + wCount + " discs", 10, 20);
            else if (bCount > wCount)
                g.drawString("Black is winning with " + bCount + " discs", 10, 20);
            else g.drawString("Currently tied", 10, 20);
        }

    }

    /**
     * The main program.
     *
     * @param args command line arguments (ignored)
     */
    public static void main(String[] args) {

        Othello content;

        if (args.length > 1) {
            System.out.println("Usage: java Othello delayTime");
            System.exit(0);
        }

        if (args.length == 1) {
            try {
                delay = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Command line arg must be an integer");
                System.exit(0);
            }
            content = new Othello(delay);
            if (delay >= 0) {
                JFrame window = new JFrame("Othello Game");
                window.setContentPane(content);
                window.setSize(530, 557);
                window.setLocation(100, 100);
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setVisible(true);
            }
        } else {
            content = new Othello();
            JFrame window = new JFrame("Othello Game");
            window.setContentPane(content);
            window.setSize(530, 557);
            window.setLocation(100, 100);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setVisible(true);
        }
    }
}  // Othello