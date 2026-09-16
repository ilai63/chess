package chess;

import java.util.List;

public class King extends Piece {
	
	public King(Color color) { //Constructor
		super(color);
	}
	

	public MoveType isValidMove(Position start, Position end, Board board, List<String[]> rawMoveList) {
	    int startRow = start.getRows();
	    int startCol = start.getColumms();
	    int endRow = end.getRows();
	    int endCol = end.getColumms();

	    // --- Out of bounds check ---
	    if (startRow < 0 || startRow >= 8 || startCol < 0 || startCol >= 8 ||
	        endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) {
	        return MoveType.INVALID;
	    }

	    Piece piece = board.getPiece(start);
	    if (piece == null) return MoveType.INVALID;
	    Color color = piece.getColor();

	    // --- Same color collision check ---
	    Piece target = board.getPiece(end);
	    if (target != null && target.getColor() == color) {
	        return MoveType.INVALID;
	    }

	    // --- KING logic ---
	    if (piece instanceof King) {
	        int rowDiff = Math.abs(startRow - endRow);
	        int colDiff = Math.abs(startCol - endCol);

	        // Normal one-square move
	        if (rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff != 0)) {
	            return MoveType.NORMAL;
	        }

	        // Castling logic
	        if (rowDiff == 0 && colDiff == 2) {
	            if (endCol > startCol && board.canKingSideCastle(color, start, rawMoveList)) {
	                return MoveType.CASTLING_KINGSIDE;
	            } else if (endCol < startCol && board.canQueenSideCastle(color, start, rawMoveList)) {
	                return MoveType.CASTLING_QUEENSIDE;
	            }
	        }

	        return MoveType.INVALID;
	    }

	    // --- Default ---
	    return MoveType.INVALID;
	}
	
	@Override
	public String getSymbol() {
		if (color == Color.BLACK) return "BK";
		return "WK";
	}
	
	@Override
	public Piece clone( ) {
		return new King(this.color);
	}
	
	@Override
	public String getSubClass() {
		return "King";
	}
	
	@Override
	public char getFENSymbol() {
		if (color == Color.BLACK) return 'k';
		return 'K';
	}
}
