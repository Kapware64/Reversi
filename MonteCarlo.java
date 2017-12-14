import java.util.*;

public class MonteCarlo {

    // How many seconds you want to run simulation for, in millisec
    private static int SIM_TIME_WHITE = 1000;
    private static int SIM_TIME_BLACK = 2000;
    private static int SIM_TIME_DEFAULT = 1000;

    // How many games to simulate
    public static int SIM_GAMES = 200000;

    public static HashMap<List, MCNode> nodeMap;

    public static GameStats stats;

    public MonteCarlo(){}

    public static Game makeMove(Game game, int color, boolean naive) {
        nodeMap = new HashMap<>();
        MCNode root = new MCNode(game, color);
        long start = System.currentTimeMillis();
        int activeSimTime = SIM_TIME_DEFAULT;
        if (color == Game.WHITE) {
            activeSimTime = SIM_TIME_WHITE;
        } else if (color == Game.BLACK) {
            activeSimTime = SIM_TIME_BLACK;
        }
        while (System.currentTimeMillis() - start < activeSimTime && root.totalSim < SIM_GAMES) {
            if (naive)
                root.naiveSimulate();
            else
                root.smartSimulate();
            if (root.children.size() == 1)
                break;
        }
        MCNode bestChild = root.getMostWinningChild();

        // Console output
//        System.out.println("Simulated " + root.totalSim + " games total. " +
//            "Simulated " + bestChild.totalSim + " games for this move and won " +
//            bestChild.wins[color] * 100 / bestChild.totalSim + "% games.");
//        int i = 0;
//        for (MCNode c : root.children) {
//            System.out.print("Child " + ++i + ": " + c.wins[color] + "/" + c.totalSim + "=" + c.wins[color] * 100 /c.totalSim + "% ");
//        }
//        int depth = root.getDepth();
//        System.out.println("Depth: " + depth);
//        System.out.println();
        System.out.println(bestChild.wins[color] * 100.0 / bestChild.totalSim);
//
//        int gameCount = stats.branchingFactors.size() - 1;
//        if (naive) {
//            stats.branchingFactors.get(gameCount).add(root.children.size());
//            stats.naiveDepths.get(gameCount).add(depth);
//            stats.naiveSims.get(gameCount).add(root.totalSim);
//            stats.confidence.get(gameCount).add(root.wins[root.turn] * 100.0 / root.totalSim);
//        }
//        else {
//            stats.smartSims.get(gameCount).add(root.totalSim);
//            stats.smartDepths.get(gameCount).add(depth);
//        }
        return bestChild.game;
    }
}
