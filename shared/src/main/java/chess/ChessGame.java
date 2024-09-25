package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    TeamColor turn;
    ChessBoard mainBoard;

    public ChessGame() {
        turn = TeamColor.WHITE;
        mainBoard = new ChessBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece currentPiece = mainBoard.getPiece(startPosition);
        ChessPiece.PieceType currentType = currentPiece.getPieceType();
        return switch(currentType){
            case KING -> new HashSet<ChessMove>(validKingMoves(currentPiece, startPosition));

            default -> new HashSet<ChessMove>(currentPiece.pieceMoves(mainBoard, startPosition));

        };
    }

    private Collection<ChessMove> validKingMoves(ChessPiece currentPiece, ChessPosition startPosition){
        ArrayList<ChessMove> moves = new ArrayList<>(currentPiece.pieceMoves(mainBoard, startPosition));
        HashSet<ChessPosition> _allPossibleMovesForOtherPieces = new HashSet<ChessPosition>();
        TeamColor currentColor = currentPiece.getTeamColor();

        for (int row = 1; row < 9; row++) { // Check for all other piece's moves;
            for (int col = 1; col < 9; col++) {
                var curPiece = mainBoard.getPiece(new ChessPosition(row, col));
                if (curPiece != null && curPiece.getPieceType() != ChessPiece.PieceType.PAWN && curPiece.getTeamColor() != mainBoard.getPiece(startPosition).getTeamColor()) {
                    var enemyMoves = curPiece.pieceMoves(mainBoard, new ChessPosition(row, col));
                    for (var move : enemyMoves) {
                        _allPossibleMovesForOtherPieces.add(move.getEndPosition());
                    }
                }
            }
        }

        ArrayList<Integer> IndexesToRemove = new ArrayList<Integer>();
        for (int i = 0; i < moves.size(); i++) { // delete moves;
            if (_allPossibleMovesForOtherPieces.contains(moves.get(i).getEndPosition())) {
                IndexesToRemove.add(i);
            }
        }

        for(int i = IndexesToRemove.size() - 1; i >=0; i--){
            moves.remove(IndexesToRemove.get(i).intValue());
        }

        for (int i = 0; i < moves.size(); i++) { // Check for pawns
            for (int j = -1; j < 2; j += 2) {
                ChessPosition positionToCheck = new ChessPosition(moves.get(i).getEndPosition());
                var piece = mainBoard.getPiece(new ChessPosition(positionToCheck.getRow() + (currentColor == TeamColor.WHITE ? - 1 : 1), positionToCheck.getColumn() + j));
                if (piece != null && piece.getTeamColor() != mainBoard.getPiece(startPosition).getTeamColor() && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                    moves.remove(i);
                    break;
                }
            }
        }

        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        {
            HashSet<ChessMove> validMoves = new HashSet<ChessMove>(validMoves(startPosition));
            if (!validMoves.contains(move)) {
                throw new InvalidMoveException("Move is not possible!");
            }
        }

        int returnCode = mainBoard.movePiece(move);
        if(returnCode != 0){
            throw new InvalidMoveException("Move is not possible on board! Error Code: " + returnCode);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        throw new RuntimeException("Not implemented");
    }

    private void changeTurn(){
        turn = turn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }
}
