package chess;

import java.util.List;

public abstract class Piece {
	protected Color color;
	protected Position position;
	
	public Piece (Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}
	
	public abstract Piece clone();
	
	//only checks if the piece can move like that, Game.isMoveValid checks that the own king isn't left in check
	public abstract MoveType isValidMove(Position start, Position end, Board board, List<String[]> rawMoveList);
	
	public abstract String getSymbol();
	
	public abstract String getSubClass();
	
	public abstract char getFENSymbol();

}
