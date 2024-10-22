package chess.rules;

public class KnightRules extends Rules {
    private static final int[][] MOVESET = {{1, 2}, {1, -2}, {-1, -2}, {-1, 2}, {2, 1}, {2, -1}, {-2, -1}, {-2, 1}};
    private static final boolean REPEATS = false;

    public KnightRules() {
        super(MOVESET, REPEATS);
    }

}
