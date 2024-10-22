package chess.rules;

public class KingRules extends Rules {
    private static final int[][] MOVESET = {{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    private static final boolean REPEATS = false;

    public KingRules() {
        super(MOVESET, REPEATS);
    }

}
