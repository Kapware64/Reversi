public class Player {
    public AIType aiType;
    private Strategy strategy;
    public int color;

    public Player(Strategy strategy, AIType aiType, int color) {
        this.aiType = aiType;
        this.strategy = strategy;
        this.color = color;
    }

    /**
     *  This method calls the appropriate strategy.
     *
     *  @param    game    the current state of the game
     *  @param    color   the color (Black or White) of the player
     *
     *  @return   game    the resulting state of the game
     */
    public Game strategy(Game game, int color) {
        if (!game.hasLegalMoves(color))
            return game;

        switch (aiType) {
            case Random:
                return strategy.randStrategy(game, color);
            case Point:
                return strategy.pointStrategy(game, color);
            case Minimax:
                return strategy.searchStrategy(game, color);
            case MonteCarlo:
                return strategy.monteCarloStrategy(game, color);
            case NaiveMonteCarlo:
                return strategy.naiveMonteCarlo(game, color);
            default:
                return strategy.randStrategy(game, color);
        }
    }
}
