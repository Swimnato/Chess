package chess.rules;

public class QueenRules extends Rules {
    private static final int[][] MOVESET = {{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    private static final boolean REPEATS = true;

    public QueenRules() {
        super(MOVESET, REPEATS);
    }

}