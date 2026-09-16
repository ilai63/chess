package chess;

import java.util.List;

public class Pawn extends Piece {

	public Pawn(Color color) { //Constructor
		super(color);
	}
	

	@Override
	public MoveType isValidMove(Position start, Position end, Board board, List<String[]> rawMoveList) {
		int startRow = start.getRows();
	    int startCol = start.getColumms();
	    int endRow = end.getRows();
	    int endCol = end.getColumms();

	    Color color = board.getPiece(start).getColor();
		
	    if (startRow < 0 || startRow >= 8 || startCol < 0 || startCol >= 8 || endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) { //out of board
	    	    return MoveType.INVALID;
	    }
	    
	    if (color == Color.WHITE) {
	    	if (startRow - 1 == endRow) {
	    		if (startCol == endCol + 1 || startCol == endCol - 1) { //Checks for pawn diagonal capture
	    			//Checks for en passent
	    			String[] lastMove = lastOpponentMove(rawMoveList, color);
	    			if (lastMove != null) {
		    			Position enemyStartPos = new Position(endRow - 1, endCol);
		    			Position enemyEndPos = new Position(endRow + 1, endCol);
		    			if (lastMove[0].equals("Pawn") && lastMove[1].equals(enemyStartPos.getPosition()) && lastMove[2].equals(enemyEndPos.getPosition())) {
		    				return MoveType.EN_PASSENT;
		    			}
	    			}
	    			
	    			if (board.getPiece(end) == null) return MoveType.INVALID;
	    	    	if (board.getPiece(end).getColor() == color) return MoveType.INVALID;
	    	    	return MoveType.NORMAL;
	    		} else if (startCol == endCol) { //Checks for pawn 1 step forward
	    			if (board.getPiece(end) == null) return MoveType.NORMAL;
	    			else return MoveType.INVALID;
	    		} else return MoveType.INVALID;
	    	} else if (startRow == 6 && endRow == 4 && startCol == endCol) { //Checks for pawn 2 steps forward from starting position
	    		if (board.getPiece(end) == null && board.getPiece(startRow - 1, endCol) == null) return MoveType.NORMAL;
	    		else return MoveType.INVALID;
	    	} else return MoveType.INVALID;
	    	
	    } else { //Opposite directions for the black pawns
	    	if (startRow + 1 == endRow) {
	    		if (startCol == endCol + 1 || startCol == endCol - 1) {
	    			//Checks for en passent
	    			String[] lastMove = lastOpponentMove(rawMoveList, color);
	    			if (lastMove != null) {
		    			Position enemyStartPos = new Position(endRow + 1, endCol);
		    			Position enemyEndPos = new Position(endRow - 1, endCol);
		    			if (lastMove[0].equals("Pawn") && lastMove[1].equals(enemyStartPos.getPosition()) && lastMove[2].equals(enemyEndPos.getPosition())) {
		    				return MoveType.EN_PASSENT;
		    			}
	    			}
	    			
	    			if (board.getPiece(end) == null) return MoveType.INVALID;
	    	    	if (board.getPiece(end).getColor() == color) return MoveType.INVALID;
	    	    	return MoveType.NORMAL;
	    		} else if (startCol == endCol) {
	    			if (board.getPiece(end) == null) return MoveType.NORMAL;
	    			else return MoveType.INVALID;
	    		} else return MoveType.INVALID;
	    	} else if (startRow == 1 && endRow == 3 && startCol == endCol) {
	    		if (board.getPiece(end) == null && board.getPiece(startRow + 1, endCol) == null) return MoveType.NORMAL;
	    		else return MoveType.INVALID;
	    	} else return MoveType.INVALID;
	   }
	}
	
	//finds the last move of the other player. the list can end with the move we're checking right now
	//(BoardPane.Move adds it first) or with a null placeholder, so those get skipped
	private static String[] lastOpponentMove(List<String[]> rawMoveList, Color color) {
		for (int i = rawMoveList.size() - 1; i >= 0 && i >= rawMoveList.size() - 2; i--) {
			String[] move = rawMoveList.get(i);
			if (move == null || move[0] == null) continue;
			if (!move[3].equals(color.getColor())) return move;
		}
		return null;
	}
	
	@Override
	public String getSymbol() {
		if (color == Color.BLACK) return "BP";
		return "WP";
	}
	
	@Override
	public Piece clone( ) {
		return new Pawn(this.color);
	}


	@Override
	public String getSubClass() {
		return "Pawn";
	}
	
	@Override
	public char getFENSymbol() {
		if (color == Color.BLACK) return 'p';
		return 'P';
	}

}
