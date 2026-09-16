package chess;

public class Position {
	private int rows;
	private int columms;
	
	public Position (int rows, int columms) { //Constructor
		this.rows = rows;
		this.columms = columms;
	}
	
	public int getRows() {
		return rows;
	}
	
	public int getColumms() {
		return columms;
	}
	
	public void setRows(int row) {
		rows = row;
	}
	
	public void setCol(int col) {
		columms = col;
	}
	
	public String getPosition() {
		String Pos = "";
		Pos +=rows;
		Pos +=columms;
		return Pos;
	}
	
	public void setPosition(String pos) {
	    this.rows = pos.charAt(0) - '0';
	    this.columms = pos.charAt(1) - '0';
	}
}
