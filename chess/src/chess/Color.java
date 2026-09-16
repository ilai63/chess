package chess;

public enum Color {
	WHITE, 
	BLACK;
	
	public String getColor() {
		switch (this) {
		case BLACK: return "black";
		case WHITE: return "white";
		default: return "no color found";
		}
	}
	
	public Color switchColors() {
		switch (this) {
		case BLACK: return Color.WHITE;
		case WHITE: return Color.BLACK;
		default: return null;
		}
	}

	public boolean isWhite() {
		switch (this) {
		case BLACK: return false;
		case WHITE: return true;
		}
		return false;
	}
}
