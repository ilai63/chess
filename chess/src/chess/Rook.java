package chess;

import java.util.List;

public class Rook extends Piece {
	
	public Rook(Color color) { //Constructor
		super(color);
	}

	@Override
	public MoveType isValidMove(Position start, Position end, Board board, List<String[]> rawMoveList) {
		int startRow = start.getRows();
	    int startCol = start.getColumms();
	    int endRow = end.getRows();
	    int endCol = end.getColumms();

	    if (startRow < 0 || startRow >= 8 || startCol < 0 || startCol >= 8 || endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) { //out of board
    	    return MoveType.INVALID;
	    }
	    
	    Color color = board.getPiece(startRow, startCol).getColor();
	    if (startRow == endRow && startCol == endCol) return MoveType.INVALID;
	    if (startRow != endRow && startCol != endCol) return MoveType.INVALID;
	    
	    if (startRow > endRow && startCol == endCol) { //Checks for row valid move
	    	for (int i = startRow - 1; i > endRow; i--) {
	    		if (board.getPiece(i, startCol) != null) return MoveType.INVALID;
	    	}
	    } else if (startRow < endRow && startCol == endCol) {
	    	for (int i = startRow + 1; i < endRow; i++) {
	    		if (board.getPiece(i, startCol) != null) return MoveType.INVALID;
	    	}
	    } else if (startCol > endCol && startRow == endRow) { //Checks for column valid move
	    	for (int i = startCol - 1; i > endCol; i--) {
	    		if (board.getPiece(startRow, i) != null) return MoveType.INVALID;
	    	}
	    } else if (startCol < endCol && startRow == endRow) {
	    	for (int i = startCol + 1; i < endCol; i++) {
	    		if (board.getPiece(startRow, i) != null) return MoveType.INVALID;
	    	}
	    } else return MoveType.INVALID;
	    if (board.getPiece(end) == null || board.getPiece(end).getColor() != color) return MoveType.NORMAL;
	    return MoveType.INVALID;
	}

	/*@Override
	public String getSymbol() {
		if (color == Color.BLACK) return "\u2656";
		return "\u265C";
	}*/
	
	@Override
	public String getSymbol() {
		if (color == Color.BLACK) return "BR";
		return "WR";
	}
	
	@Override
	public Piece clone( ) {
		return new Rook(this.color);
	}
	
	@Override
	public String getSubClass() {
		return "Rook";
	}
	
	@Override
	public char getFENSymbol() {
		if (color == Color.BLACK) return 'r';
		return 'R';
	}

}
