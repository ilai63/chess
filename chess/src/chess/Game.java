package chess;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Game {
	private Board board;
	private Color currentPlayer;
	public List<String> moveList = new ArrayList<>();
	//every move is saved as {piece, from, to, color, capture}
	//squares are saved as "row col" (row 0 is black's back rank, col 0 is the a file), so e2 is "64"
	public static List<String[]> rawMoveList = new ArrayList<>();
	
	public Game() { //Constructor
		
		board = new Board();
		currentPlayer = Color.WHITE;
	}
	
	public static void move (Position start, Position end, Board board) {
		
		Piece piece = board.getPiece(start);
		board.setPiece(end, piece);
		board.setPiece(start, null);
	}
	
	public static void undoMove(Position start, Position end, Board board, Piece capturedPiece) {
	    Piece piece = board.getPiece(end);
	    board.setPiece(start, piece);

	    board.setPiece(end, capturedPiece);
	}
	
	public int[] interprateMove (String move, int[] moveArr) {
		
		moveArr[0] = 8 - (move.charAt(1) - 48); //number
		moveArr[1] = move.charAt(0) - 97; //letter
		moveArr[2] = 8 - (move.charAt(move.length() - 1) - 48); //number
		moveArr[3] = move.charAt(move.length() - 2) - 97; //letter
		
		return moveArr;
	}
	
	public MoveType isMoveValid (Position start, Position end, Color color, Board board, List<String[]> rawMoveList) {
		
		Piece piece = board.getPiece(start); //gets the piece in the starting position
		
		if (piece == null || piece.getColor() != color) return MoveType.INVALID;
		MoveType type = piece.isValidMove(start, end, board, rawMoveList);
	    if (type == MoveType.INVALID) return MoveType.INVALID;
	    
		Board testBoard = board.clonedBoard();
		move(start, end, testBoard);
		if (type == MoveType.EN_PASSENT) testBoard.setPiece(start.getRows(), end.getColumms(), null); //the pawn captured en passent is removed too
		Position kingPos = testBoard.getKingPosition(color);
		if (isCheck(color, testBoard, kingPos)) return MoveType.INVALID; //checks if the move causes own king to be in check (illegal)
		if (piece instanceof Pawn) {
			int endRow = end.getRows();
		    if ((piece.getColor() == Color.WHITE && endRow == 0) ||
		        (piece.getColor() == Color.BLACK && endRow == 7)) {
		        type = MoveType.PROMOTION;
		    }
		}
		return type;
	}
	
	public void start() {
		
		//starts the game
		board.resetBoard();
		Scanner scanner = new Scanner(System.in);
		turn(scanner);
	}
	
	public void turn (Scanner in) {
		
		while (true) { 
			
			System.out.println("");
			board.printBoard();
			System.out.println("");
			if (currentPlayer == Color.WHITE) System.out.println("White to move"); //prints instructions
			else System.out.println("Black to move");
			System.out.println("Enter move like - (a2 - a4)");
			
			String move = in.nextLine().trim().replaceAll(" ", "");
			if (!move.matches("[a-h][1-8]-[a-h][1-8]")) { // Checks if input is correct
			    System.out.println("Invalid format");
			    continue;
			}
			
			int[] moveArr = new int[4]; //make the move into 2 position classes
			interprateMove(move, moveArr);
			Position start = new Position(moveArr [0],moveArr[1]);
			Position end = new Position(moveArr [2],moveArr[3]);
			String[] rawMove = writeRawMove(start, end, currentPlayer, board);
			rawMoveList.add(rawMove);
			MoveType type = isMoveValid(start, end, currentPlayer, board, rawMoveList);
			if (type != MoveType.INVALID) { //checks if the move is valid
				if (type == MoveType.CASTLING_KINGSIDE) castleKingSide(currentPlayer, board);
				if (type == MoveType.CASTLING_QUEENSIDE) castleQueenSide(currentPlayer, board);
				if (type == MoveType.EN_PASSENT) enPassent(end, currentPlayer, board);
				String moveString = writeMove(start, end);
				move(start, end, board);
				moveList.add(moveString);
				
				Piece piece = board.getPiece(end);
				if (piece instanceof Pawn) { //check for promotion situation (before checking for mate, so a promotion that mates counts)
					if (piece.getColor() == Color.BLACK && end.getRows() == 7) promotion(end, piece, in);
					if (piece.getColor() == Color.WHITE && end.getRows() == 0) promotion(end, piece, in);
				}
				
				currentPlayer = currentPlayer.switchColors();
				if (isCheckmate(currentPlayer, board)) {
					Color winner;
					if (currentPlayer == Color.BLACK) winner = Color.WHITE;
					else winner = Color.BLACK;
					board.printBoard();
					System.out.println("\nCheckmate! \n" + winner.getColor() + " Wins");
					break;
				} else if (isDraw(rawMoveList, board, currentPlayer)) {
					board.printBoard();
					System.out.println("\nDraw");
					break;
				}
				
			} else {
				rawMoveList.remove(rawMoveList.getLast());
				System.out.println("Move is not valid");
			}
		}
		
	}
	
	public static boolean isCheck(Color color, Board board, Position kingPos) {
	    if (kingPos.getRows() == -1) return false; //king not found

	    for (int row = 0; row < 8; row++) {
	        for (int col = 0; col < 8; col++) {
	            Piece attacker = board.getPiece(row, col);
	            if (attacker == null || attacker.getColor() == color) continue;
	            
	            if (attacker instanceof Pawn) { //if pawn diagonal attacks
	            	int dir = (attacker.getColor() == Color.WHITE) ? -1 : 1; // white pawns move up, black down
	                if (kingPos.getRows() == row + dir && Math.abs(kingPos.getColumms() - col) == 1) {
	                    return true;
	                }
	            } else if (attacker.isValidMove(new Position(row, col), kingPos, board, rawMoveList) != MoveType.INVALID) //if any other piece attacks
	                return true;
	        }
	    }
	    return false;
	}
	
	public void promotion(Position position, Piece piece, Scanner in) {
		
		System.out.println("Promote to Queen, Rook, Bishop, Knight");
		String choice = in.nextLine().toLowerCase().trim();
		Piece promoted;
		switch (choice) { //check input to see to which sub class to promote
			case "queen": promoted = new Queen(piece.getColor());
						  board.setPiece(position, promoted);
						  break;
			case "rook": promoted = new Rook(piece.getColor());
			  			 board.setPiece(position, promoted);
			  			 break;
			case "bishop": promoted = new Bishop(piece.getColor());
						   board.setPiece(position, promoted);
						   break;
			case "knight": promoted = new Knight(piece.getColor());
						   board.setPiece(position, promoted);
						   break;
			default : System.out.println("invalid input");
					  promotion(position, piece, in);
					  break;
		}
	}
	
	public boolean isCheckmate(Color color, Board board) {
		return isCheckmate(color, board, rawMoveList);
	}
	
	//moves = all the moves played so far, needed for en passent
	public boolean isCheckmate(Color color, Board board, List<String[]> moves) {
	    // Try to move king to escape
		
		Position kingPos = board.getKingPosition(color);
	    int[] dirRow = {-1, -1, -1, 0, 0, 1, 1, 1};
	    int[] dirCol = {-1, 0, 1, -1, 1, -1, 0, 1};
	    List<Position> attackers = allAttacker(kingPos, color, board);
	    
	    if (attackers.isEmpty()) return false;

	    for (int i = 0; i < 8; i++) {
	        int newRow = kingPos.getRows() + dirRow[i];
	        int newCol = kingPos.getColumms() + dirCol[i];
	        if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
	            Position newPos = new Position(newRow, newCol);
	            if (isMoveValid(kingPos, newPos, color, board, moves) != MoveType.INVALID) return false;
	        }
	    }
	    
	    if (attackers.size() > 1) {
	    	return true;
	    }
	    
	    // Try to capture or block attacker
	    Piece attacker = board.getPiece(attackers.get(0));
	    int attackerRow = attackers.get(0).getRows();
	    int attackerCol = attackers.get(0).getColumms();

	    for (int row = 0; row < 8; row++) {
	        for (int col = 0; col < 8; col++) {
	            Piece defender = board.getPiece(row, col);
	            Position defenderPos = new Position(row, col);
	            
	            if (defender != null && defender.getColor() == color) {
	                if (isMoveValid(defenderPos, attackers.get(0), color, board, moves) != MoveType.INVALID) return false;
	                // king can not block (Position has no equals so compare the numbers)
	                if (row == kingPos.getRows() && col == kingPos.getColumms()) continue;

	                // Bishop check block
	                if (attacker instanceof Bishop) {
	                    int rowDir = Integer.compare(attackerRow, kingPos.getRows());
	                    int colDir = Integer.compare(attackerCol, kingPos.getColumms());

	                    int interRow = kingPos.getRows() + rowDir;
	                    int interCol = kingPos.getColumms() + colDir;

	                    while (interRow != attackerRow && interCol != attackerCol) {
	                        Position interception = new Position(interRow, interCol);
	                        if (isMoveValid(defenderPos, interception, color, board, moves) != MoveType.INVALID) return false;
	                        interRow += rowDir;
	                        interCol += colDir;
	                    }
	                    // rook check block
	                } else if (attacker instanceof Rook) {
	                    if (attackerCol == kingPos.getColumms()) {
	                        int minRow = Math.min(attackerRow, kingPos.getRows());
	                        int maxRow = Math.max(attackerRow, kingPos.getRows());

	                        for (int r = minRow + 1; r < maxRow; r++) {
	                            Position interception = new Position(r, attackerCol);
	                            if (isMoveValid(defenderPos, interception, color, board, moves) != MoveType.INVALID) return false;
	                        }
	                    } else if (attackerRow == kingPos.getRows()) {
	                        int minCol = Math.min(attackerCol, kingPos.getColumms());
	                        int maxCol = Math.max(attackerCol, kingPos.getColumms());

	                        for (int c = minCol + 1; c < maxCol; c++) {
	                            Position interception = new Position(attackerRow, c);
	                            if (isMoveValid(defenderPos, interception, color, board, moves) != MoveType.INVALID) return false;
	                        }
	                    }
	                } else if (attacker instanceof Queen) {
	                    // Try bishop style block
	                    int rowDiff = Math.abs(attackerRow - kingPos.getRows());
	                    int colDiff = Math.abs(attackerCol - kingPos.getColumms());
	                    if (rowDiff == colDiff) {
	                        int rowDir = Integer.compare(attackerRow, kingPos.getRows());
	                        int colDir = Integer.compare(attackerCol, kingPos.getColumms());

	                        int interRow = kingPos.getRows() + rowDir;
	                        int interCol = kingPos.getColumms() + colDir;

	                        while (interRow != attackerRow && interCol != attackerCol) {
	                            Position interception = new Position(interRow, interCol);
	                            if (isMoveValid(defenderPos, interception, color, board, moves) != MoveType.INVALID) return false;
	                            interRow += rowDir;
	                            interCol += colDir;
	                        }
	                    }
	                    // Try rook style block
	                    if (attackerRow == kingPos.getRows()) {
	                        int minCol = Math.min(attackerCol, kingPos.getColumms());
	                        int maxCol = Math.max(attackerCol, kingPos.getColumms());
	                        for (int c = minCol + 1; c < maxCol; c++) {
	                            Position interception = new Position(attackerRow, c);
	                            if (isMoveValid(defenderPos, interception, color, board, moves) != MoveType.INVALID) return false;
	                        }
	                    } else if (attackerCol == kingPos.getColumms()) {
	                        int minRow = Math.min(attackerRow, kingPos.getRows());
	                        int maxRow = Math.max(attackerRow, kingPos.getRows());
	                        for (int r = minRow + 1; r < maxRow; r++) {
	                            Position interception = new Position(r, attackerCol);
	                            if (isMoveValid(defenderPos, interception, color, board, moves) != MoveType.INVALID) return false;
	                        }
	                    }
	                }
	            }
	        }
	    }

	    // a pawn that just moved 2 squares and gives check can also be taken en passent
	    if (attacker instanceof Pawn) {
	    	int passedRow = (color == Color.WHITE) ? attackerRow - 1 : attackerRow + 1; // the square the pawn jumped over
	    	for (int side = -1; side <= 1; side += 2) {
	    		int col = attackerCol + side;
	    		if (col < 0 || col > 7) continue;
	    		Piece pawn = board.getPiece(attackerRow, col);
	    		if (pawn instanceof Pawn && pawn.getColor() == color &&
	    			isMoveValid(new Position(attackerRow, col), new Position(passedRow, attackerCol), color, board, moves) == MoveType.EN_PASSENT) return false;
	    	}
	    }

	    // checkmate
	    return true;
	}
	
	public List<Position> allAttacker(Position position, Color color, Board board) {
	    List<Position> attackers = new ArrayList<>();

	    for (int rows = 0; rows < 8; rows++) {
	        for (int cols = 0; cols < 8; cols++) {
	            Piece piece = board.getPiece(rows, cols);

	            // Skip if empty or same color
	            if (piece == null || piece.getColor() == color) continue;

	            Position possibleAttacker = new Position(rows, cols);

	            // can it reach the king? isValidMove and not isMoveValid, because a pinned piece still gives check
	            if (piece.isValidMove(possibleAttacker, position, board, rawMoveList) != MoveType.INVALID) {
	                attackers.add(possibleAttacker);
	            }
	        }
	    }

	    return attackers;
	}
	
	public String writeMove(Position start, Position end) {
		String move = "";
		Piece piece = board.getPiece(start);
		move += piece.getSubClass() + " from ";
		move += start.getPosition();
		move += " to " + end.getRows() + end.getColumms();
		return move;
	}
	
	public String[] writeRawMove(Position start, Position end, Color color, Board board) {
		Piece piece = board.getPiece(start);
		String[] move  = new String[5];
		if (piece == null) return move;
		move[0] = piece.getSubClass(); // piece type
		move[1] = start.getPosition(); // piece starting position
		move[2] = end.getPosition(); // piece ending position
		move[3] = color.getColor(); // piece color
		if (board.getPiece(end) == null) move[4] = "false"; // capture?
		else move[4] = "true";
		return move;
	}
	
	public boolean isDraw(List<String[]> moves, Board board, Color color) {
	    if (isDeadPosition(board)) return true;
	    if (isStalemate(color, board, moves)) return true;
	    if (is50MoveRule(moves)) return true;
	    if (isRepetition(moves, board)) return true;
	    return false;
	}
	
	public boolean isStalemate(Color color, Board board, List<String[]> rawMoveList) {
		if (isCheck(color, board, board.getKingPosition(color))) return false;
		
	    for (int row = 0; row < 8; row++) {
	        for (int col = 0; col < 8; col++) {
	            Piece piece = board.getPiece(row, col);
	            if (piece != null && piece.getColor() == color) {

	                Position start = new Position(row, col);

	                for (int r = 0; r < 8; r++) {
	                    for (int c = 0; c < 8; c++) {
	                        Position end = new Position(r, c);

	                        if (start.getPosition().equals(end.getPosition())) continue;

	                        if (isMoveValid(start, end, color, board, rawMoveList) != MoveType.INVALID) {
	                        	return false;
	                        }
	                    }
	                }
	            }
	        }
	    }

	    // No legal moves and not in check → stalemate
	    return true;
	}
	
	public boolean isDeadPosition(Board board) {

	    int knights = 0;
	    int lightBishops = 0, darkBishops = 0; // bishops of both players, counted by the color of their square

	    for (int r = 0; r < 8; r++) {
	        for (int c = 0; c < 8; c++) {
	            Piece p = board.getPiece(r, c);
	            if (p == null || p instanceof King) continue;
	            if (p instanceof Bishop) {
	                if ((r + c) % 2 == 0) lightBishops++;
	                else darkBishops++;
	            } else if (p instanceof Knight) {
	                knights++;
	            } else {
	            	return false; // there is a pawn/rook/queen -> not dead by simple insufficient-material rules
	            }
	        }
	    }

	    // K vs K, K+B vs K, K+B vs K+B, K+B+B vs K... when all the bishops are on the same square color nobody can be mated
	    if (knights == 0 && (lightBishops == 0 || darkBishops == 0)) return true;

	    // K+N vs K
	    if (knights == 1 && lightBishops + darkBishops == 0) return true;

	    return false;
	}
	
	public boolean is50MoveRule(List<String[]> rawMoveList) {
		//50 moves by each player = 100 entries in rawMoveList
		int noPawnOrCaptureMoves = 0; 
		for (int i = rawMoveList.size() - 1; i >= 0 && noPawnOrCaptureMoves < 100; i--) { 
			if (rawMoveList.get(i)[0].equals("Pawn") || rawMoveList.get(i)[4].equals("true")) return false;
			noPawnOrCaptureMoves++; 
		}
		
		if (noPawnOrCaptureMoves >= 100) return true; 
		return false;
	}
	
	public boolean isRepetition(List<String[]> rawMoveList, Board board) {
		//threefold repetition = the same position 3 times, not the same moves
		//a position can't come back after a pawn move, capture or castling, so we go back move by move on a copy of the board and compare
		Board testBoard = board.clonedBoard();
		String currentPosition = positionKey(testBoard, rawMoveList, rawMoveList.size());
		int repetitions = 1;
		
		for (int i = rawMoveList.size() - 1; i >= 0; i--) {
			String[] move = rawMoveList.get(i);
			if (move == null || move[0] == null) break;
			boolean castling = move[0].equals("King") && Math.abs(move[1].charAt(1) - move[2].charAt(1)) == 2;
			if (move[0].equals("Pawn") || move[4].equals("true") || castling) break;
			
			//undo the move (it wasn't a capture, so the end square was empty before)
			testBoard.movePiece(move[2].charAt(0) - '0', move[2].charAt(1) - '0', move[1].charAt(0) - '0', move[1].charAt(1) - '0');
			if (positionKey(testBoard, rawMoveList, i).equals(currentPosition)) repetitions++;
		}
		
		return repetitions >= 3;
	}
	
	//turns a position into a string: the pieces, whose turn it is, castling rights and en passent
	private String positionKey(Board board, List<String[]> rawMoveList, int movesPlayed) {
		StringBuilder key = new StringBuilder();
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				Piece piece = board.getPiece(row, col);
				key.append(piece == null ? '.' : piece.getFENSymbol());
			}
		}
		key.append(movesPlayed % 2 == 0 ? " w " : " b ");
		key.append(getCastlingRights(rawMoveList, movesPlayed));
		
		//if en passent can really be played right now, it's not the same position as the one without it
		if (movesPlayed > 0) {
			String[] lastMove = rawMoveList.get(movesPlayed - 1);
			if (lastMove != null && "Pawn".equals(lastMove[0]) && Math.abs(lastMove[1].charAt(0) - lastMove[2].charAt(0)) == 2) {
				int row = lastMove[2].charAt(0) - '0';
				int col = lastMove[2].charAt(1) - '0';
				int passedRow = (lastMove[1].charAt(0) - '0' + row) / 2;
				boolean canTake = false;
				for (int side = -1; side <= 1; side += 2) {
					if (col + side < 0 || col + side > 7) continue;
					Piece neighbor = board.getPiece(row, col + side);
					if (neighbor instanceof Pawn && !neighbor.getColor().getColor().equals(lastMove[3]) &&
						isMoveValid(new Position(row, col + side), new Position(passedRow, col), neighbor.getColor(), board, rawMoveList.subList(0, movesPlayed)) == MoveType.EN_PASSENT) canTake = true;
				}
				if (canTake) key.append(" ep").append(col);
			}
		}
		return key.toString();
	}
	
	//castling rights after the first movesPlayed moves, written like in FEN ("KQkq", "Kq", "-"...)
	//a right is gone once the king or that rook moves, or something lands on the rook's square (it got captured)
	public static String getCastlingRights(List<String[]> rawMoveList, int movesPlayed) {
		boolean whiteKingSide = true, whiteQueenSide = true, blackKingSide = true, blackQueenSide = true;
		
		for (int i = 0; i < movesPlayed && i < rawMoveList.size(); i++) {
			String[] move = rawMoveList.get(i);
			if (move == null || move[0] == null) continue;
			for (String square : new String[] {move[1], move[2]}) {
				if (square.equals("74")) { whiteKingSide = false; whiteQueenSide = false; } //white king e1
				if (square.equals("77")) whiteKingSide = false; //white rook h1
				if (square.equals("70")) whiteQueenSide = false; //white rook a1
				if (square.equals("04")) { blackKingSide = false; blackQueenSide = false; } //black king e8
				if (square.equals("07")) blackKingSide = false; //black rook h8
				if (square.equals("00")) blackQueenSide = false; //black rook a8
			}
		}
		
		String rights = (whiteKingSide ? "K" : "") + (whiteQueenSide ? "Q" : "") + (blackKingSide ? "k" : "") + (blackQueenSide ? "q" : "");
		return rights.isEmpty() ? "-" : rights;
	}
	
	public void castleQueenSide(Color color, Board board) {
		if (color == Color.WHITE) { 
			board.movePiece(7, 0, 7, 3);
		}
		if (color == Color.BLACK) {
			board.movePiece(0, 0, 0, 3);
		}
	}
	
	public void castleKingSide(Color color, Board board) {
		if (color == Color.WHITE) {
			board.movePiece(7, 7, 7, 5);
		}
		if (color == Color.BLACK) {
			board.movePiece(0, 7, 0, 5);
		}
	}
	
	public void enPassent(Position pos, Color color, Board board) {
		if (color == Color.WHITE) board.setPiece(pos.getRows() + 1, pos.getColumms(), null);
		if (color == Color.BLACK) board.setPiece(pos.getRows() - 1, pos.getColumms(), null);
	}
}
