package chess;

import java.util.List;

public class Knight extends Piece {

	public Knight(Color color) { //Constructor
		super(color);
	}

	@Override
	public MoveType isValidMove(Position start, Position end, Board board, List<String[]> rawMoveList) {
		int startRow = start.getRows();
	    int startCol = start.getColumms();
	    int endRow = end.getRows();
	    int endCol = end.getColumms();

	    Color color = board.getPiece(startRow, startCol).getColor();
	    
	    if (startRow < 0 || startRow >= 8 || startCol < 0 || startCol >= 8 || endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) { //out of board
    	    return MoveType.INVALID;
	    }
	    
	    if (board.getPiece(endRow, endCol) == null || board.getPiece(endRow, endCol).getColor() != color) { //Checks if end square is empty or occupied by rival piece
	    	if ((startRow + 2 == endRow || startRow - 2 == endRow) && (startCol + 1 == endCol || startCol - 1 == endCol))return MoveType.NORMAL; //checks for correct movement
	    	else if ((startRow + 1 == endRow || startRow - 1 == endRow) && (startCol + 2 == endCol || startCol - 2 == endCol))return MoveType.NORMAL;
	    	else return MoveType.INVALID;
	    } else return MoveType.INVALID;
	}

	/*@Override
	public String getSymbol() {
		if (color == Color.BLACK) return "\u2658";
		return "\u265E";
	}*/
	
	@Override
	public String getSymbol() {
		if (color == Color.BLACK) return "BN";
		return "WN";
	}
	
	@Override
	public Piece clone( ) {
		return new Knight(this.color);
	}

	@Override
	public String getSubClass() {
		return "Knight";
	}
	
	@Override
	public char getFENSymbol() {
		if (color == Color.BLACK) return 'n';
		return 'N';
	}

}
