package chess.rules;

public class RookRules extends Rules {
    private static final int[][] moveset = {{1, 0}, {0, -1}, {-1, 0}, {0, 1}};
    private static final boolean repeats = true;

    public RookRules() {
        super(moveset, repeats);
    }

}