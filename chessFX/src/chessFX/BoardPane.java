package chessFX;

import java.util.ArrayList;
import java.util.List;
import chess.*;
import engine.EngineResponse;
import engine.FENUtils;
import engine.StockfishAPI;
import java.util.Random;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;


public class BoardPane {
	
	private GridPane grid;
    private StackPane[][] squares;
    private Board board;
    
    private final int BOARD_SIZE = 8;
    private final int TILE_SIZE = 60;
    //the address can be changed with -DchessApiUrl=ws://... (for testing with a local server)
    private static StockfishAPI api = new StockfishAPI(System.getProperty("chessApiUrl", "wss://chess-api.com/v1"));
    //how long the bot waits for the engine. if there is no answer (no internet) it plays a random legal move so the game doesn't get stuck
    private static final long BOT_WAIT_MS = Long.getLong("botWaitMs", 8000);
    private Game game = new Game();
    
    private List<Board> positions = new ArrayList<>(); //the board after every move, index 0 is the start. used by the review screen
    private List<String> fens = new ArrayList<>(); //the FEN that was sent to the engine for every position (the game's last position isn't sent)
    private boolean gameOver = false;
    private boolean waitingForPromotion = false;
    private boolean reviewing = false;
    private String endText = ""; //why the game ended ("Mate", "Draw", "Time"), shown at the end of the review
    private int gameNumber = 0; //goes up every new game, so a late engine answer from an old game is ignored
    
    public BoardPane() {
    	this.board = new Board();
    	this.grid = new GridPane();
    	this.squares = new StackPane[BOARD_SIZE][BOARD_SIZE];
    }
    
    public GridPane getGrid() {
        return grid;
    }
    
    //the engine's answer for the position after moveNumber moves (null if it didn't answer yet)
    public EngineResponse getEvaluation(int moveNumber) {
    	String fen = moveNumber < fens.size() ? fens.get(moveNumber) : null;
    	return api.getAnswer(gameNumber + "-" + moveNumber, fen);
    }
    
    //sends the current position to the engine. the taskId (game number + move number) and the fen come back with the answer
    private void sendPosition(chess.Color player, List<String[]> rawMoveList) {
    	String fen = FENUtils.boardToFEN(board, player, rawMoveList);
    	fens.add(fen);
    	api.sendFen(fen, gameNumber + "-" + rawMoveList.size(), 1, 15, 50);
    }
    
    public boolean isGameOver() {
    	return gameOver;
    }
    
    //no moves while the game is over, a promotion piece is being chosen or the review screen is open
    public boolean isInputBlocked() {
    	return gameOver || waitingForPromotion || reviewing;
    }
    
    //called from EndingUI when the game ends in any way
    public void endGame() {
    	gameOver = true;
    	waitingForPromotion = false;
    	PromotionUI.close(); //the time can run out while the promotion window is open
    }
    
    public void setEndText(String text) {
    	endText = text;
    }
    
    public String getEndText() {
    	return endText;
    }
    
    public void setReviewing(boolean reviewing) {
    	this.reviewing = reviewing;
    }
    
    public int getPositionCount() {
    	return positions.size();
    }
    
    public Board getPosition(int index) {
    	return positions.get(index);
    }
    
    public Board getBoard() {
    	return board;
    }
    
    public StackPane[][] getBoardMat () {
    	return squares;
    }
    
    //starts a new game (also used by "Play Again")
    public void setup() {
    	gameNumber++;
    	gameOver = false;
    	waitingForPromotion = false;
    	PromotionUI.close();
    	reviewing = false;
    	endText = "";
        board.resetBoard();
        DragAndDrop.reset();
        positions.clear();
        positions.add(board.clonedBoard());
        fens.clear();
        buildBoard();
        
        sendPosition(chess.Color.WHITE, DragAndDrop.getMoveList());
        botMove(chess.Color.WHITE); //does nothing unless the bot plays white
    }

    
    public void highlightValidSquares(Position start, chess.Color color, List<String[]> rawMoveList) {
	    for (int row = 0; row < 8; row++) {
	        for (int col = 0; col < 8; col++) {
	            StackPane square = squares[row][col];
	            
	            if (isOnBoard(row, col) && game.isMoveValid(start, new Position(row, col), color, board, rawMoveList) != MoveType.INVALID) {
	                if ((row + col) % 2 == 0) {
	                    square.setStyle("-fx-background-color: " + Visual.VALID_LIGHT_COLOR + ";");
	                } else {
	                    square.setStyle("-fx-background-color: " + Visual.VALID_DARK_COLOR + ";");
	                }
	                Visual.setBorders(square, 1.7);
	            }
	        }
	    }
	    // castling squares get highlighted by the loop too (King.isValidMove checks castling)
        
	    StackPane square = squares[start.getRows()][start.getColumms()];
		square.setStyle("-fx-background-color: " + Visual.SELECTED_COLOR + ";");
		Visual.setBorders(square, 1.7);
	}
    
    public Position getPositionFromSquare(StackPane square) {
	    Integer row = GridPane.getRowIndex(square) - 1;
	    Integer col = GridPane.getColumnIndex(square) - 1;

	    // GridPane returns null if row/col were not explicitly set → treat as 0
	    return new Position(row == null ? 0 : row, col == null ? 0 : col);
	}
    
    public boolean isOnBoard(int row, int col) {
	    return row >= 0 && row < 8 && col >= 0 && col < 8;
	}
    
	public void setPieces(String active) {
		drawPieces(board, active);
	}
	
	//draws the pieces of a board, active = which pieces can be dragged ("both", "white", "black" or "none")
	public void drawPieces(Board source, String active) {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
            	squares[row][col].getChildren().clear();
                Piece piece = source.getPiece(row, col);
                if (piece != null) {
                    ImageView pieceView = Visual.createPieceView(piece, 56);
                    pieceView.setPickOnBounds(false);
                    squares[row][col].getChildren().add(pieceView);
                    
                    if (active.equals("both"))  {
                    	DragAndDrop.makeDraggable(pieceView, this);
                    	Visual.addHoverTranslate(pieceView, 2, 100);
                    }
                    else if (active.equals("white")) {
                    	if (piece.getColor() == chess.Color.BLACK) {
                    		DragAndDrop.makeDraggable(pieceView, this);
                    		Visual.addHoverTranslate(pieceView, 2, 100);
                    	}
                    } else if (active.equals("black")) {
                    	if (piece.getColor() == chess.Color.WHITE) {
                    		DragAndDrop.makeDraggable(pieceView, this);
                    		Visual.addHoverTranslate(pieceView, 2, 100);
                    	}
                    }
                    
                    pieceView.setSmooth(false);
                } else {
                	squares[row][col].getChildren().clear();
                }
            }
        }
    }
	
	public void buildBoard() {
        grid.getChildren().clear();

        for (int row = 0; row < BOARD_SIZE + 2; row++) {
            for (int col = 0; col < BOARD_SIZE + 2; col++) {
                if (row == 0 || row == BOARD_SIZE + 1) {
                    // Top & bottom letters
                    if (col > 0 && col < BOARD_SIZE + 1) {
                        char letter = (char) ('a' + col - 1);
                        Label label = new Label(String.valueOf(letter));
                        Visual.setLabel(label, Visual.THALEAH, Color.WHITE, TILE_SIZE / 2, TILE_SIZE, Pos.CENTER);
                        grid.add(label, col, row);
                    }
                } else if (col == 0 || col == BOARD_SIZE + 1) {
                    // Left & right numbers
                    if (row > 0 && row < BOARD_SIZE + 1) {
                        int number = BOARD_SIZE - (row - 1);
                        Label label = new Label(String.valueOf(number));
                        Visual.setLabel(label, Visual.THALEAH, Color.WHITE, TILE_SIZE, TILE_SIZE / 2, Pos.CENTER);
                        grid.add(label, col, row);
                    }
                } else {
                    // Chess squares
                    StackPane square = new StackPane();
                    square.setPrefSize(TILE_SIZE, TILE_SIZE);
                    

                    if ((row + col) % 2 == 0) {
                        square.setStyle("-fx-background-color: " + Visual.LIGHT_COLOR + ";");
                    } else {
                        square.setStyle("-fx-background-color: " + Visual.DARK_COLOR + ";");
                    }
                    
                    Visual.setBorders(square, 1.7);
                    DragAndDrop.makeDroppable(square);
                    squares[row - 1][col - 1] = square;
                    grid.add(square, col, row);
                }
            }
        }
        setPiecesAccordingToSettings();
    }
	
	public void setPiecesAccordingToSettings() {
		if (MainUI.settings.isBotEnabled()) {
        	setPieces(MainUI.settings.getBotTurn().getColor());
        } else setPieces("both");
	}
	
	public void highlightMove(String[] lastMove, String color) {

	    int startPos = Integer.parseInt(lastMove[1]);
	    int endPos = Integer.parseInt(lastMove[2]);

	    StackPane start = squares[startPos / 10][startPos % 10];
	    StackPane end = squares[endPos / 10][endPos % 10];

	    start.setStyle("-fx-background-color: " + color + ";");
	    Visual.setBorders(start, 1.7);
	    end.setStyle("-fx-background-color: " + color + ";");
	    Visual.setBorders(end, 1.7);
	}
	
	public void clearBoardColors() {
		for (int row = 0; row < 8; row++) {
	        for (int col = 0; col < 8; col++) {
	            StackPane square = squares[row][col];

	            // Reset to original color
	            if ((row + col) % 2 == 0) {
	                square.setStyle("-fx-background-color: " + Visual.LIGHT_COLOR + ";");
	            } else {
	                square.setStyle("-fx-background-color: " + Visual.DARK_COLOR + ";");
	            }
	            Visual.setBorders(square, 1.7);
	        }
		}
	}
	
	public chess.Color Move(chess.Color player, List<String[]> rawMoveList, Position startPos, Position targetPos) {
		return Move(player, rawMoveList, startPos, targetPos, null);
	}
	
	//promotionPiece: 'q', 'r', 'b' or 'n', null = the player picks it in PromotionUI first
	public chess.Color Move(chess.Color player, List<String[]> rawMoveList, Position startPos, Position targetPos, Character promotionPiece) {
		if (gameOver || waitingForPromotion) return player;
		
		//updates moveList
		String[] rawMove = game.writeRawMove(startPos, targetPos, player, getBoard());
		rawMoveList.add(rawMove);
		
		//gets move type. an illegal move is never played (for example an old answer from the engine)
		MoveType type = game.isMoveValid(startPos, targetPos, player, getBoard(), rawMoveList);
		if (type == MoveType.INVALID) {
			rawMoveList.remove(rawMoveList.size() - 1);
			return player;
		}

		//a promotion is only played after the player picks the piece, so the clock keeps running while choosing
		//and nothing is left half done if the time runs out or the game is restarted
		if (type == MoveType.PROMOTION && promotionPiece == null) {
			rawMoveList.remove(rawMoveList.size() - 1);
			waitingForPromotion = true;
			int thisGame = gameNumber;
			PromotionUI.show(player, piece -> {
				if (thisGame != gameNumber || gameOver) return;
				waitingForPromotion = false;
				DragAndDrop.player = Move(player, rawMoveList, startPos, targetPos, Character.toLowerCase(piece.getFENSymbol()));
			});
			return player;
		}
		clearBoardColors();
		chess.Color nextPlayer = player.switchColors();
		
		//adds captured pieces to capture box
		Piece captured = getBoard().getPiece(targetPos);
        if (rawMoveList.get(rawMoveList.size() - 1)[4].equals("true")) {
        	SoundManager.playCapture();
            if (captured.getColor() == chess.Color.WHITE) MainUI.whiteCapturedBox.addCapturedPiece(captured, MainUI.blackCapturedBox);
            else MainUI.blackCapturedBox.addCapturedPiece(captured, MainUI.whiteCapturedBox);
        }
        
        //moves the piece on the logic board
        getBoard().movePiece(startPos, targetPos);
        
        //handles move according to type
        if (type == MoveType.EN_PASSENT) {
            // Remove the captured pawn correctly
            Position capturedPawnPos = new Position(startPos.getRows(), targetPos.getColumms()); // Pawn is on the same row as start, column of target
            Piece capturedPawn = getBoard().getPiece(capturedPawnPos);
            game.enPassent(targetPos, player, getBoard()); // Removes it from board
            SoundManager.playCapture();
            if (capturedPawn.getColor() == chess.Color.WHITE)
                MainUI.whiteCapturedBox.addCapturedPiece(capturedPawn, MainUI.blackCapturedBox);
            else
                MainUI.blackCapturedBox.addCapturedPiece(capturedPawn, MainUI.whiteCapturedBox);
        } else if (type == MoveType.CASTLING_KINGSIDE) {
            game.castleKingSide(player, getBoard());
            SoundManager.playCastle();
        } else if (type == MoveType.CASTLING_QUEENSIDE) {
            SoundManager.playCastle();
            game.castleQueenSide(player, getBoard());
        } else if (type == MoveType.PROMOTION) {
            SoundManager.playPromotion();
            getBoard().setPiece(targetPos, promotedPiece(promotionPiece, player));
        } else if (type == MoveType.NORMAL) {
            SoundManager.playMove();
        }

        //switches timer if enabled
        if (MainUI.settings.isTimerEnabled()) MainUI.whiteTimer.onMoveMade(player.isWhite(), MainUI.blackTimer);
        Visual.updateTurnLabel(nextPlayer);

        finishMove(nextPlayer, rawMoveList);

        return nextPlayer;
	}

	//everything that happens after the move is on the board
	private void finishMove(chess.Color player, List<String[]> rawMoveList) {
		positions.add(board.clonedBoard());
		checkGameState(player, rawMoveList);
		
        board.printBoard();
        //updates the UI after everything
        setPiecesAccordingToSettings();
        
        //highlights the move
        highlightMove(rawMoveList.get(rawMoveList.size() - 1), "#ffe375");
        
        if (!gameOver) {
        	sendPosition(player, rawMoveList); //every position gets evaluated, for the bot and for the review screen
        	botMove(player);
        }
	}
	
	//checks for game scenarios and handles accordingly
	private void checkGameState(chess.Color player, List<String[]> rawMoveList) {
		Position kingPos = getBoard().getKingPosition(player);
        if (game.isCheckmate(player, getBoard(), rawMoveList)) {
        	endText = "Mate";
        	MainUI.turnLabel.setText("");
        	MainUI.checkLabel.setText("");
        	SoundManager.playEnd();
            EndingUI.show(player, rawMoveList);
        } else if (game.isDraw(rawMoveList, getBoard(), player)) { //before "Check!" because 50 moves and repetition count in check too
        	endText = "Draw";
        	MainUI.turnLabel.setText("");
        	MainUI.checkLabel.setText("");
        	SoundManager.playEnd();
            EndingUI.show(null, rawMoveList);
        } else if (Game.isCheck(player, getBoard(), kingPos)) {
            SoundManager.playCheck();
            MainUI.checkLabel.setText("Check!");
        } else {
        	MainUI.checkLabel.setText("");
        }
	}
	
	private Piece promotedPiece(char piece, chess.Color color) {
		switch (piece) {
			case 'r': return new Rook(color);
			case 'b': return new Bishop(color);
			case 'n': return new Knight(color);
			default: return new Queen(color);
		}
	}
	
	public void botMove(chess.Color player) {
	    if (!MainUI.settings.isBotEnabled() || MainUI.settings.getBotTurn() != player || gameOver) return;
	    
	    int thisGame = gameNumber;
	    List<String[]> moves = DragAndDrop.getMoveList();
	    int moveNumber = moves.size();
	    String taskId = thisGame + "-" + moveNumber;
	    String fen = fens.get(fens.size() - 1); //finishMove just sent this position
	    long start = System.currentTimeMillis();
	    
	    Thread thinking = new Thread(() -> {
	    	//waits at least a second, and until the engine answered this exact position (the old code took the last answer, even for an older position)
	    	EngineResponse answer = null;
	    	while (System.currentTimeMillis() - start < BOT_WAIT_MS) {
	    		answer = api.getAnswer(taskId, fen);
	    		if (answer != null && System.currentTimeMillis() - start >= 1000) break;
	    		try {
	    			Thread.sleep(50);
	    		} catch (InterruptedException ignored) {}
	    	}
	    	EngineResponse finalAnswer = answer;
	    	
	        Platform.runLater(() -> {
	        	//the game could have ended or restarted while waiting
	        	if (thisGame != gameNumber || gameOver || moves.size() != moveNumber) return;
	        	
	        	Position[] move = engineMove(finalAnswer);
	        	char promotion = (finalAnswer != null && finalAnswer.move != null && finalAnswer.move.length() == 5) ? finalAnswer.move.charAt(4) : 'q';
	        	if (move == null || game.isMoveValid(move[0], move[1], player, getBoard(), moves) == MoveType.INVALID) {
	        		System.out.println("No usable answer from the engine, the bot plays a random legal move");
	        		move = randomLegalMove(player, moves);
	        		promotion = 'q';
	        		if (move == null) return;
	        	}
	        	DragAndDrop.player = Move(player, moves, move[0], move[1], promotion);
	        });
	    });
	    thinking.setDaemon(true); //so it doesn't keep the program open after Quit
	    thinking.start();
	}
	
	//the engine sends the move like "e2e4" or "e7e8q"
	private Position[] engineMove(EngineResponse answer) {
		if (answer == null || answer.move == null || answer.move.length() < 4) return null;
		String m = answer.move;
		Position start = new Position(8 - (m.charAt(1) - '0'), m.charAt(0) - 'a');
		Position end = new Position(8 - (m.charAt(3) - '0'), m.charAt(2) - 'a');
		if (!isOnBoard(start.getRows(), start.getColumms()) || !isOnBoard(end.getRows(), end.getColumms())) return null;
		return new Position[]{start, end};
	}
	
	private Position[] randomLegalMove(chess.Color player, List<String[]> moves) {
		List<Position[]> legal = new ArrayList<>();
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				Piece piece = board.getPiece(row, col);
				if (piece == null || piece.getColor() != player) continue;
				for (int r = 0; r < 8; r++) {
					for (int c = 0; c < 8; c++) {
						Position from = new Position(row, col);
						Position to = new Position(r, c);
						if ((row != r || col != c) && game.isMoveValid(from, to, player, board, moves) != MoveType.INVALID) legal.add(new Position[]{from, to});
					}
				}
			}
		}
		if (legal.isEmpty()) return null;
		return legal.get(new Random().nextInt(legal.size()));
	}

}
