package chess.rules;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class PawnRules extends Rules{
    private static final int[][] fakeMoveset = {{1}};

    public PawnRules(){
        super(fakeMoveset,false); // some random values, we are overriding everything anyways;
    }

    @Override
    public Collection<ChessMove> generateMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece myself = board.getPiece(myPosition);
        HashSet<ChessMove> output = new HashSet<ChessMove>();
        ChessGame.TeamColor myColor = myself.getTeamColor();
        int direction = myColor == ChessGame.TeamColor.WHITE? 1 : -1;
        int row = myPosition.getRow() + direction;
        int col = myPosition.getColumn();
        ChessPosition newPos = new ChessPosition(row,col);
        if(!newPos.isValid(board)){
            return output;
        }
        ChessPiece pieceAtPos = board.getPiece(newPos);
        Boolean isAPromotion = (myColor == ChessGame.TeamColor.WHITE? row == board.getRows() : row == 1);
        //Check for forward movement(s)
        if(newPos.isValid(board) && pieceAtPos == null){
            if(isAPromotion) {
                output.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.ROOK));
                output.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.KNIGHT));
                output.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.QUEEN));
                output.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.BISHOP));
            }
            else{
                output.add(new ChessMove(myPosition, newPos, null));
                if(row - direction == ((myColor == ChessGame.TeamColor.WHITE) ? 2 : (board.getRows() - 1))){
                    row += direction;
                    newPos = new ChessPosition(row,col);
                    pieceAtPos = board.getPiece(newPos);
                    if(pieceAtPos == null){
                        output.add(new ChessMove(myPosition, newPos, null));
                    }
                }
            }
        }

        //Check for capturing enemies
        for(byte side = -1; side < 2; side += 2){
            row = myPosition.getRow() + direction;
            col = myPosition.getColumn() + side;
            newPos = new ChessPosition(row,col);
            if(newPos.isValid(board)) {
                pieceAtPos = board.getPiece(newPos);
                isAPromotion = (myColor == ChessGame.TeamColor.WHITE ? row == board.getRows() : row == 1);
                if (pieceAtPos != null && pieceAtPos.getTeamColor() != myColor) {
                    if (isAPromotion) {
                        output.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.ROOK));
                        output.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.KNIGHT));
                        output.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.QUEEN));
                        output.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.BISHOP));
                    } else {
                        output.add(new ChessMove(myPosition, newPos, null));
                    }
                }
            }
        }

        return output;
    }
}
