package chess.rules;

public class KingRules extends Rules {
    private static final int[][] moveset = {{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    private static final boolean repeats = false;

    public KingRules() {
        super(moveset, repeats);
    }

}
