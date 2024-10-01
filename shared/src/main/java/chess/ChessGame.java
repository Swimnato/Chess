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
        TeamColor currentColor = currentPiece.getTeamColor();
        ArrayList<ChessMove> output = new ArrayList<>();
        ArrayList<ChessMove> pieceMoves = switch(currentType){
            case KING -> new ArrayList<>(validKingMoves(currentPiece, startPosition));
            default -> new ArrayList<>(currentPiece.pieceMoves(mainBoard, startPosition));
        };
        for(var move : pieceMoves){
            ChessBoard boardBackup = mainBoard.cloneBoard();
            mainBoard.movePiece(move);
            if(!isInCheck(currentColor)){
                output.add(move);
            }
            mainBoard = boardBackup;
        }

        return output;
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
        ChessPiece currentKing = null;
        ChessPosition kingPos = null;
        byte x = 1;
        byte y = 1;
        while(currentKing == null || currentKing.getTeamColor() != teamColor || currentKing.getPieceType() != ChessPiece.PieceType.KING) {
            kingPos = new ChessPosition(x,y);
            currentKing = mainBoard.getPiece(kingPos);
            x++;
            if(x > mainBoard.getRows()){
                x = 1;
                y++;
            }
            if(y > mainBoard.getCols()){
                throw new RuntimeException("No " + teamColor + " King Exists!");
            }
        }
        HashSet<ChessPosition> _allPossibleMovesForOtherPieces = new HashSet<ChessPosition>();

        for (int row = 1; row < 9; row++) { // Check for all the other piece's moves;
            for (int col = 1; col < 9; col++) {
                var curPiece = mainBoard.getPiece(new ChessPosition(row, col));
                if (curPiece != null && curPiece.getTeamColor() != teamColor) {
                    if(curPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
                        var enemyMoves = curPiece.pieceMoves(mainBoard, new ChessPosition(row, col));
                        for (var move : enemyMoves) {
                            _allPossibleMovesForOtherPieces.add(move.getEndPosition());
                        }
                    }
                    else{
                        ChessPosition l = new ChessPosition(row + (teamColor == TeamColor.WHITE ? -1 : 1), col + 1);
                        ChessPosition r = new ChessPosition(row + (teamColor == TeamColor.WHITE ? -1 : 1), col - 1);
                        if(l.isValid(mainBoard))
                            _allPossibleMovesForOtherPieces.add(l);
                        if(r.isValid(mainBoard))
                            _allPossibleMovesForOtherPieces.add(r);
                    }
                }
            }
        }

        return _allPossibleMovesForOtherPieces.contains(kingPos);
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
        mainBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return mainBoard;
    }

    private void changeTurn(){
        turn = turn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }
}
