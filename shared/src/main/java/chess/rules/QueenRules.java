package chess.rules;

public class QueenRules extends Rules {
    private static final int[][] moveset = {{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    private static final boolean repeats = true;

    public QueenRules() {
        super(moveset, repeats);
    }

}