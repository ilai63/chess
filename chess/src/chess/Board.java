package chess;

import java.util.List;

public class Board {
	//board[row][col], row 0 is the 8th rank (black's side) and col 0 is the a file
	private Piece[][] board;
	public Board() { //Constructor
		board = new Piece[8][8];
		resetBoard();
	}
	
	public void resetBoard() {
		
		for (int row = 0; row < 8; row++) {
	        for (int col = 0; col < 8; col++) {
	            board[row][col] = null;
	        }
	    }
		
		for (int col = 0; col < 8; col++) { //organizing the pawns
			board[1][col] = new Pawn(Color.BLACK);
			board[6][col] = new Pawn(Color.WHITE);
		}
		
		board[0][0] = new Rook(Color.BLACK);
		board[0][7] = new Rook(Color.BLACK);
		board[0][6] = new Knight(Color.BLACK);
		board[0][5] = new Bishop(Color.BLACK);
		board[0][4] = new King(Color.BLACK);
		board[0][3] = new Queen(Color.BLACK);
		board[0][2] = new Bishop(Color.BLACK);
		board[0][1] = new Knight(Color.BLACK);
		
		board[7][0] = new Rook(Color.WHITE);
		board[7][7] = new Rook(Color.WHITE);
		board[7][6] = new Knight(Color.WHITE);
		board[7][5] = new Bishop(Color.WHITE);
		board[7][4] = new King(Color.WHITE);
		board[7][3] = new Queen(Color.WHITE);
		board[7][2] = new Bishop(Color.WHITE);
		board[7][1] = new Knight(Color.WHITE);
		
	}
	
	public void printBoard () {
		
		System.out.println("  a b c d e f g h");
		
		for (int row = 0; row < 8; row++) {
			System.out.print(8 - row + " ");
	        for (int col = 0; col < 8; col++) {
	            if (board[row][col] == null) System.out.print(". ");
	            else System.out.print(board[row][col].getSymbol());
	        }
	        System.out.println(8 - row);
	    }
		
		System.out.println("  a b c d e f g h");
	}
	
	public Piece getPiece(Position position) {
		
		int row = position.getRows();
	    int col = position.getColumms();
		return board[row][col];
	}
	
	public Piece getPiece(int row, int col) {
		
		return board[row][col];
	}
	
	public void setPiece(Position position, Piece piece) {
		
		int row = position.getRows();
	    int col = position.getColumms();
		board[row][col] = piece;
	}
	
	public void setPiece(int row, int col, Piece piece) {
		
		board[row][col] = piece;
	}
	
	public void movePiece(Position start, Position end) {
		Piece piece = board[start.getRows()][start.getColumms()];
		board[end.getRows()][end.getColumms()] = piece;
		board[start.getRows()][start.getColumms()] = null;
	}
	
	public Position getKingPosition(Color color) {

		for (int row = 0; row < 8; row++) {
	        for (int col = 0; col < 8; col++) {
	        	
	        	Piece piece = board[row][col];
	            if(piece != null && piece instanceof King && piece.getColor() == color) { //checks if the piece is the right king
	            	return new Position(row, col);
	            }
	        }
	    }
		
		return new Position(-1,-1); //king not found
	}
	
	public Board clonedBoard() {
		
		Board testBoard = new Board();
		
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				
				Piece piece = board[row][col];
				if (piece != null) {
					testBoard.board[row][col] = piece.clone();
				} else {
					testBoard.board[row][col] = null;
				}
			}
		}
		
		return testBoard; //Creates a copy of the board
	}

	public void movePiece(int startRow, int startCol, int endRow, int endCol) {
		Piece piece = board[startRow][startCol];
		board[endRow][endCol] = piece;
		board[startRow][startCol] = null;
	}
	
	public boolean canKingSideCastle (chess.Color color, Position start, List<String[]> rawMoveList) {
		return canCastle(color, start, rawMoveList, 7);
	}
	
	public boolean canQueenSideCastle (chess.Color color, Position start, List<String[]> rawMoveList) {
		return canCastle(color, start, rawMoveList, 0);
	}
	
	//all the castling rules are here (King calls it). rookCol is 7 for king side and 0 for queen side
	private boolean canCastle (chess.Color color, Position start, List<String[]> rawMoveList, int rookCol) {
		int homeRow = (color == Color.WHITE) ? 7 : 0;
		
		//king and rook have to be on their starting squares
		if (start.getRows() != homeRow || start.getColumms() != 4) return false;
		Piece king = getPiece(homeRow, 4);
		Piece rook = getPiece(homeRow, rookCol);
		if (!(king instanceof King) || king.getColor() != color) return false;
		if (!(rook instanceof Rook) || rook.getColor() != color) return false;
		
		//no castling if the king or this rook already moved, or the rook got captured on its square
		//BoardPane.Move adds the move to the list before checking it, so if the last move is ours we skip it
		int movesPlayed = rawMoveList.size();
		String[] lastMove = movesPlayed > 0 ? rawMoveList.get(movesPlayed - 1) : null;
		if (lastMove != null && lastMove[0] != null && lastMove[3].equals(color.getColor())) movesPlayed--;
		char right = (rookCol == 7) ? 'K' : 'Q';
		if (color == Color.BLACK) right = Character.toLowerCase(right);
		if (Game.getCastlingRights(rawMoveList, movesPlayed).indexOf(right) == -1) return false;
		
		//squares between the king and the rook have to be empty
		int step = (rookCol == 7) ? 1 : -1;
		for (int col = 4 + step; col != rookCol; col += step) {
			if (getPiece(homeRow, col) != null) return false;
		}
		
		//can't castle out of check, through check or into check
		Position passSquare = new Position(homeRow, 4 + step);
		Position endSquare = new Position(homeRow, 4 + 2 * step);
		return !Game.isCheck(color, this, start) && !Game.isCheck(color, this, passSquare) && !Game.isCheck(color, this, endSquare);
	}
	
}
