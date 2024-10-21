package chess.rules;

public class KnightRules extends Rules {
    private static final int[][] moveset = {{1, 2}, {1, -2}, {-1, -2}, {-1, 2}, {2, 1}, {2, -1}, {-2, -1}, {-2, 1}};
    private static final boolean repeats = false;

    public KnightRules() {
        super(moveset, repeats);
    }

}
