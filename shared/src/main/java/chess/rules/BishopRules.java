package chess.rules;

public class BishopRules extends Rules{
    private static final int[][] moveset = {{1,1}, {1,-1}, {-1,-1}, {-1,1}};
    private static final boolean repeats = true;

    public BishopRules(){
        super(moveset,repeats);
    }

}