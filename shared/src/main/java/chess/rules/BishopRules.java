package chess.rules;

public class BishopRules extends Rules {
    private static final int[][] MOVESET = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
    private static final boolean REPEATS = true;

    public BishopRules() {
        super(MOVESET, REPEATS);
    }

}