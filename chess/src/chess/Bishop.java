package chess;

import java.util.List;

public class Bishop extends Piece {

	public Bishop(Color color) { //Constructor
		super(color);
	}

	@Override
	public MoveType isValidMove(Position start, Position end, Board board, List<String[]> rawMoveList) {
		int startRow = start.getRows();
	    int startCol = start.getColumms();
	    int endRow = end.getRows();
	    int endCol = end.getColumms();
	    
	    int rowDiff = Math.abs(startRow - endRow);
	    int colDiff = Math.abs(startCol - endCol);
	    Color color = board.getPiece(start).getColor();
	    
	    if (startRow < 0 || startRow >= 8 || startCol < 0 || startCol >= 8 || endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) { //out of board
    	    return MoveType.INVALID;
	    }
	    
	    if (rowDiff != colDiff || rowDiff == 0) return MoveType.INVALID;
	    if (startRow > endRow && startCol > endCol) { //Checks for left up diagonal
	    	for (int i = 1; i < rowDiff; i++) {
	    		if (board.getPiece(startRow - i, startCol - i) != null) return MoveType.INVALID;
	    	}
	    } else if (startRow < endRow && startCol < endCol) { //Checks for right down diagonal
	    	for (int i = 1; i < rowDiff; i++) {
	    		if (board.getPiece(startRow + i, startCol + i) != null) return MoveType.INVALID;
	    	}
	    } else if (startRow > endRow && startCol < endCol) { //Checks for right up diagonal
	    	for (int i = 1; i < rowDiff; i++) {
	    		if (board.getPiece(startRow - i, startCol + i) != null) return MoveType.INVALID;
	    	}
	    } else if (startRow < endRow && startCol > endCol) { //Checks for left down diagonal
	    	for (int i = 1; i < rowDiff; i++) {
	    		if (board.getPiece(startRow + i, startCol - i) != null) return MoveType.INVALID;
	    	}
	    } else return MoveType.INVALID;
	    if (board.getPiece(end) == null || board.getPiece(end).getColor() != color) return MoveType.NORMAL;
	    return MoveType.INVALID;
	}

	/*@Override
	public String getSymbol() {
		if (color == Color.BLACK) return "\u2657";
		return "\u265D";
	}*/
	
	@Override
	public String getSymbol() {
		if (color == Color.BLACK) return "BB";
		return "WB";
	}
	
	@Override
	public Piece clone( ) {
		return new Bishop(this.color);
	}
	
	@Override
	public String getSubClass() {
		return "Bishop";
	}
	
	@Override
	public char getFENSymbol() {
		if (color == Color.BLACK) return 'b';
		return 'B';
	}

}
