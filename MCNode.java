import java.util.*;

public class MCNode {
    /**
     * Exploration parameter for UCT calculation
     */
    private final static double EXP_PARAM = Math.sqrt(2);

    /**
     * Total simulation counts
     */
    public int totalSim;

    /**
     * Number of wins in simulations for each color
     */
    public int[] wins;

    /**
     * Game state
     */
    public Game game;

    /**
     * Whose turn at this point of the game
     */
    public int turn;

    /**
     * List of child nodes in Monte Carlo tree
     */
    public HashSet<MCNode> children;

    public ArrayList<MCNode> parents;

    public MCNode(Game game, int turn) {
        this.game = game;
        this.turn = turn;
        totalSim = 0;
        wins = new int[Othello.PLAYER_SIZE];
        children = new HashSet<>();
        parents = new ArrayList<>();
    }

    /**
     * Run basic Monte Carlo simulation beginning from current node
     *
     * @return
     */
    public int naiveSimulate() {
        int opponent = Game.toggleTurn(turn);
        int winner;
        if (children.isEmpty()) {
            if (totalSim == 0) {
                winner = rollout();
            } else {
                ArrayList<int[]> legalMoves = game.getLegalMoves(turn);
                int nextTurn = opponent;
                if (legalMoves.isEmpty()) {
                    // If current player has no move, check opponent's moves
                    legalMoves = game.getLegalMoves(opponent);
                    if (legalMoves.isEmpty()) {
                        // If opponent has no moves, this node is a terminal state
                        winner = game.getWinner();
                        return winner;
                    }
                    nextTurn = turn;
                }
                // Expansion phase: add a child for each of next move
                for (int[] move : legalMoves) {
                    Game gameCopy = new Game();
                    gameCopy.board = Util.deepCopy(game.board);
                    gameCopy.placeDisk(move[0], move[1], turn);
                    MCNode child = new MCNode(gameCopy, nextTurn);
                    children.add(child);
                }
                winner = children.iterator().next().naiveSimulate();
            }
        } else {
            MCNode bestChild = getMaxUCTChild();
            winner = bestChild.naiveSimulate();
        }
        totalSim++;
        if (winner == Game.BLACK || winner == Game.WHITE)
            wins[winner]++;
        return winner;
    }

    public MCNode getMaxUCTChild() {
        double maxUCT = 0;
        MCNode bestChild = null;
        for (MCNode child : children) {
            double uct = uct(child);
            if (bestChild == null || uct > maxUCT) {
                maxUCT = uct;
                bestChild = child;
            }
        }
        return bestChild;
    }

    public MCNode getMostWinningChild() {
        double maxWin = -1;
        MCNode bestChild = null;
        for (MCNode child : children) {
            double win = child.wins[turn] * 1.0 / child.totalSim;
            if (win > maxWin) {
                maxWin = win;
                bestChild = child;
            }
        }
        return bestChild;
    }

    /**
     * Upper Confidence Bound 1 applied to trees, highest UCT child will be chosen for exploration
     *
     * @param child child node
     * @return UCT value for a child
     */
    private double uct(MCNode child) {
        if (child.totalSim == 0)
            return Double.MAX_VALUE;
        return child.wins[turn] * 1.0 / child.totalSim + EXP_PARAM * Math.sqrt(Math.log(totalSim) / child.totalSim);
    }

    /**
     * Randomly plays a game from the current board state and see if player wins
     *
     * @return winner player
     */
    public int rollout() {
        Game gameCopy = new Game();
        gameCopy.board = Util.deepCopy(game.board);
        int player = turn;
        int opponent = Game.toggleTurn(player);
        ArrayList<int[]> legalMoves;
        while (true) {
            legalMoves = gameCopy.getLegalMoves(player);

            if (!legalMoves.isEmpty()) {
                // If there is a legal move, randomly make a move
                Random rand = new Random();
                int[] move = legalMoves.get(rand.nextInt(legalMoves.size()));
                gameCopy.placeDisk(move[0], move[1], player);
            } else {
                // Check terminal condition
                boolean opponentHasMove = !gameCopy.getLegalMoves(opponent).isEmpty();
                if (!opponentHasMove)
                    return gameCopy.getWinner();
            }
//            Util.printBoard(gameCopy.board);
            // Switch turn
            opponent = player;
            player = Game.toggleTurn(player);
        }
    }

    /**
     * Generate all four possible board orientations by rotating 90 deg
     *
     * @param board game board
     * @return four board orientations
     */
    public static int[][][] allBoardOrientations(int[][] board) {
        final int N = board.length;
        int[][][] allBoards = new int[8][][];
        allBoards[0] = board;
        // Consider all rotations
        for (int i = 1; i < 4; i++) {
            allBoards[i] = new int[N][N];
            // Consider all squares one by one
            for (int x = 0; x < N / 2; x++) {
                // Consider elements in group of 4 in current square
                for (int y = x; y < N - x - 1; y++) {
                    allBoards[i][x][y] = allBoards[i - 1][y][N - 1 - x];
                    allBoards[i][y][N - 1 - x] = allBoards[i - 1][N - 1 - x][N - 1 - y];
                    allBoards[i][N - 1 - x][N - 1 - y] = allBoards[i - 1][N - 1 - y][x];
                    allBoards[i][N - 1 - y][x] = allBoards[i - 1][x][y];
                }
            }
        }
        // Consider all mirror images
        for (int i = 4; i < 8; i++) {
            allBoards[i] = new int[N][N];
            for (int x = 0; x < N; x++)
                for (int y = 0; y < N; y++)
                    allBoards[i][x][y] = allBoards[i - 4][N - x - 1][y];
        }
        return allBoards;
    }

    /**
     * Monte Carlo simulation with node merging, simulate about 50% more games
     */
    public void smartSimulate() {
        MCNode selectedNode = this;
        // Selection phase: select most promising child
        while (!selectedNode.children.isEmpty()) {
            selectedNode = selectedNode.getMaxUCTChild();
        }
        int winner = -1;
        int childTotalSim = 0;
        int[] childWins = new int[Othello.PLAYER_SIZE];
        if (selectedNode.totalSim == 0) {
            // Simulation phase
            winner = selectedNode.rollout();
        }
        else {
            HashSet<MCNode> children = selectedNode.children;
            int turn = selectedNode.turn;
            int opponent = Game.toggleTurn(turn);
            int nextTurn = opponent;
            Game game = selectedNode.game;
            ArrayList<int[]> legalMoves = game.getLegalMoves(turn);
            if (legalMoves.isEmpty()) {
                legalMoves = game.getLegalMoves(opponent);
                nextTurn = turn;
                if (legalMoves.isEmpty()) {
                    // neither player has legal moves, terminal state
                    winner = game.getWinner();
                }
            }
            // Expansion phase: create children
            if (winner == -1) {
                for (int[] move : legalMoves) {
                    Game gameCopy = new Game();
                    gameCopy.board = Util.deepCopy(game.board);
                    gameCopy.placeDisk(move[0], move[1], turn);
                    int[][][] allBoards = allBoardOrientations(gameCopy.board);
                    boolean duplicate = false;
                    for (int i = 0; i < allBoards.length; i++) {
                        List<Integer> list = Util.toFlatList(allBoards[i]);
                        if (MonteCarlo.nodeMap.containsKey(list)) {
                            MCNode sameNode = MonteCarlo.nodeMap.get(list);
                            if (sameNode.turn == nextTurn) {
                                if (!children.contains(sameNode)) {
                                    children.add(sameNode);
                                    sameNode.parents.add(selectedNode);
                                    childTotalSim = sameNode.totalSim;
                                    for (int j = 0; j < childWins.length; j++)
                                        childWins[j] = sameNode.wins[j];
                                }
                                duplicate = true;
                                break;
                            }
                        }
                    }
                    if (!duplicate) {
                        MCNode child = new MCNode(gameCopy, nextTurn);
                        children.add(child);
                        child.parents.add(selectedNode);
                        List<Integer> key = Util.toFlatList(gameCopy.board);
                        MonteCarlo.nodeMap.put(key, child);
//                Util.printBoard(child.game.board);
                    }
                }
                selectedNode = selectedNode.getMaxUCTChild();
                winner = selectedNode.rollout();
            }
        }

        // Propagation phase: propagate winner statistics to parents
        if (winner == Game.BLACK || winner == Game.WHITE)
            childWins[winner]++;
        childTotalSim++;
        Queue<MCNode> nodes = new LinkedList<>();
        nodes.add(selectedNode);
        while (!nodes.isEmpty()) {
            MCNode n = nodes.poll();
            n.totalSim += childTotalSim;
            for (int i = 0; i < n.wins.length; i++)
                n.wins[i] += childWins[i];
            for (MCNode e : n.parents)
                nodes.add(e);
        }
    }

    /**
     * Finds maximum depth of Monte Carlo tree
     * @return Depth of Monte Carlo tree
     */
    public int getDepth() {
        if (children.isEmpty())
            return 0;
        int maxDepth = 0;
        for (MCNode child : children) {
            int d = child.getDepth();
            if (maxDepth < d)
                maxDepth = d;
        }
        return 1 + maxDepth;
    }
}
