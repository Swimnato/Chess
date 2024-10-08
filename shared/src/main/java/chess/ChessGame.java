package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    TeamColor turn;
    ChessBoard mainBoard;
    ChessBoard previousBoard;

    public ChessGame() {
        turn = TeamColor.WHITE;
        mainBoard = new ChessBoard();
        mainBoard.resetBoard();
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
        if(currentPiece == null){
            return null;
        }
        ChessPiece.PieceType currentType = currentPiece.getPieceType();
        TeamColor currentColor = currentPiece.getTeamColor();
        ArrayList<ChessMove> output = new ArrayList<>();
        ArrayList<ChessMove> pieceMoves = switch(currentType){
            case KING -> new ArrayList<>(validKingMoves(currentPiece, startPosition));
            case PAWN -> new ArrayList<>(validPawnMoves(currentPiece, startPosition));
            default -> new ArrayList<>(currentPiece.pieceMoves(mainBoard, startPosition));
        };
        for(var move : pieceMoves){
            ChessBoard boardBackup = new ChessBoard(mainBoard);
            mainBoard.movePiece(move);
            if(!isInCheck(currentColor)){
                output.add(move);
            }
            mainBoard = new ChessBoard(boardBackup);
        }

        return output;
    }

    private Collection<ChessMove> validKingMoves(ChessPiece currentPiece, ChessPosition startPosition){
        ArrayList<ChessMove> moves = new ArrayList<>(currentPiece.pieceMoves(mainBoard, startPosition));
        ArrayList<ChessMove> output = new ArrayList<>();
        for(var move : moves){
            ChessBoard backupBoard = new ChessBoard(mainBoard);
            mainBoard.movePiece(move);
            if(!isInCheck(currentPiece.getTeamColor())){
                output.add(move);
            }
            mainBoard = new ChessBoard(backupBoard);
        }

        //check for castling
        if(currentPiece.isFirstMove() && !isInCheck(currentPiece.getTeamColor())){
            for(short direction = -1; direction < 2; direction += 2){
                int row = startPosition.getRow();
                int col = startPosition.getColumn();
                ChessPosition checkedPosition = (new ChessPosition(row,col));
                for(col = startPosition.getColumn(); checkedPosition.isValid(mainBoard); col += direction){
                    checkedPosition = (new ChessPosition(row,col));
                    if(checkedPosition.isValid(mainBoard)){
                        if(mainBoard.getPiece(checkedPosition) != null){
                            if((mainBoard.getPiece(checkedPosition).getPieceType() != ChessPiece.PieceType.ROOK && mainBoard.getPiece(checkedPosition).getPieceType() != ChessPiece.PieceType.KING ) || !mainBoard.getPiece(checkedPosition).isFirstMove()){
                                break;
                            }
                            else{
                                if(mainBoard.getPiece(checkedPosition).getPieceType() == ChessPiece.PieceType.ROOK && mainBoard.getPiece(checkedPosition).isFirstMove()) {
                                    ChessBoard backupBoard = new ChessBoard(mainBoard);

                                    ChessPosition firstMove = new ChessPosition(row, startPosition.getColumn() + direction);
                                    mainBoard.movePiece(new ChessMove(startPosition, firstMove, null));
                                    if (!isInCheck(currentPiece.getTeamColor())) {
                                        ChessPosition secondMove = new ChessPosition(row, startPosition.getColumn() + direction * 2);
                                        mainBoard.movePiece(new ChessMove(startPosition, secondMove, null));
                                        if (!isInCheck(currentPiece.getTeamColor())) {
                                            output.add(new ChessMove(startPosition, secondMove, null));
                                        }
                                    }
                                    mainBoard = new ChessBoard(backupBoard);
                                }
                            }
                        }
                    }
                }
            }
        }

        return output;
    }

    private Collection<ChessMove> validPawnMoves(ChessPiece currentPiece, ChessPosition startPosition){
        var currentMoveset = new ArrayList<>(currentPiece.pieceMoves(mainBoard, startPosition));
        var row = startPosition.getRow();
        var col = startPosition.getColumn();
        int direction = (currentPiece.getTeamColor() == TeamColor.WHITE ? 1 : -1);

        //now we check for enpassant
        for(short side = -1; side < 2; side += 2){
            ChessPosition pieceToSidePos = new ChessPosition(row,col + side);
            if(pieceToSidePos.isValid(mainBoard)) {
                ChessPiece pieceToSide = mainBoard.getPiece(pieceToSidePos);
                if (pieceToSide != null && pieceToSide.getPieceType() == ChessPiece.PieceType.PAWN && pieceToSide.getTeamColor() != currentPiece.getTeamColor()) { // if there is an enemy pawn to our side
                    ChessPiece previousPieceToSide = previousBoard.getPiece(pieceToSidePos);
                    if (previousPieceToSide == null) {
                        ChessPosition spaceToSideBackAStepPos = new ChessPosition(row + (direction * 2), col + side);
                        if(spaceToSideBackAStepPos.isValid(mainBoard)){
                            ChessPiece spaceToSideBackAStep = previousBoard.getPiece(spaceToSideBackAStepPos);
                            if (spaceToSideBackAStep != null && spaceToSideBackAStep.getTeamColor() != currentPiece.getTeamColor() && spaceToSideBackAStep.getPieceType() == ChessPiece.PieceType.PAWN) { // to see if the pawn was back two spaces before
                                ChessBoard backupPrevious = new ChessBoard(previousBoard);
                                backupPrevious.movePiece(new ChessMove(spaceToSideBackAStepPos, pieceToSidePos, null));
                                if (backupPrevious.equals(mainBoard)) { // to make sure this was the previous move
                                    currentMoveset.add(new ChessMove(startPosition, new ChessPosition(row + direction, col + side), null));
                                }
                            }
                        }
                    }
                }
            }
        }

        return currentMoveset;
    }
    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessBoard soonToBePreviousBoard = new ChessBoard(mainBoard);
        ChessPosition startPosition = move.getStartPosition();
        if(mainBoard.getPiece(startPosition) == null){
            throw new InvalidMoveException("No piece to move!");
        }
        if(mainBoard.getPiece(startPosition).getTeamColor() != turn){
            throw new InvalidMoveException("Not your color!");
        }
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
        turn = turn == TeamColor.WHITE ? TeamColor.BLACK: TeamColor.WHITE;
        previousBoard = new ChessBoard(soonToBePreviousBoard);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = kingPosition(teamColor);
        if(!kingPos.isValid(mainBoard)){
            return false;
        }
        int x = kingPos.getRow();
        int y = kingPos.getColumn();

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


    private ChessPosition kingPosition(TeamColor teamColor){
        ChessPiece currentKing = null;
        ChessPosition kingPos = null;
        byte x = 1;
        byte y = 1;
        while(currentKing == null || currentKing.getTeamColor() != teamColor || currentKing.getPieceType() != ChessPiece.PieceType.KING) {
            kingPos = new ChessPosition(x,y);
            if(!kingPos.isValid(mainBoard)){
                return new ChessPosition(0,0);
                /*   Originally I made this code to throw an exception, because there should never be
                a chess game with no king, but several of the test cases have no kings, so to make
                the tests run I had to get rid of this :(                  */
                //throw new RuntimeException("No " + teamColor + " King Exists!");
            }
            currentKing = mainBoard.getPiece(kingPos);
            x++;
            if(x > mainBoard.getRows()){
                x = 1;
                y++;
            }
        }
        return kingPos;
    }

    private Collection<ChessMove> getAllTeamMoves(TeamColor teamColor){
        var moves = new ArrayList<ChessMove>();
        for(int row = 1; row <= mainBoard.getRows(); row++){
            for(int col = 1; col <= mainBoard.getRows(); col++){
                ChessPosition currentPosition = new ChessPosition(row,col);
                ChessPiece currentPiece = mainBoard.getPiece(currentPosition);
                if(currentPiece != null && currentPiece.getTeamColor() == teamColor){
                    var pieceMoves = new ArrayList<ChessMove>(validMoves(currentPosition));
                    moves.addAll(pieceMoves);
                }
            }
        }
        return moves;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        ChessPosition kingPos = kingPosition(teamColor);
        if(!kingPos.isValid(mainBoard)){
            return false;
        }
        var moves = getAllTeamMoves(teamColor);

        return (isInCheck(teamColor) && moves.isEmpty());
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if(isInCheck(teamColor)){
            return false;
        }
        ChessPosition kingPos = kingPosition(teamColor);
        if(!kingPos.isValid(mainBoard)){
            return false;
        }
        var moves = getAllTeamMoves(teamColor);
        return moves.isEmpty();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return turn == chessGame.turn && Objects.equals(mainBoard, chessGame.mainBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, mainBoard);
    }
}
