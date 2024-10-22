package chess.rules;

public class RookRules extends Rules {
    private static final int[][] MOVESET = {{1, 0}, {0, -1}, {-1, 0}, {0, 1}};
    private static final boolean REPEATS = true;

    public RookRules() {
        super(MOVESET, REPEATS);
    }

}