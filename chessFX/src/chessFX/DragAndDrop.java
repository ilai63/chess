package chessFX;

import java.util.ArrayList;
import java.util.List;
import chess.*;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;

public class DragAndDrop {
    
    public static Color player = Color.WHITE;
    private static Position dragStartPos;
    private static List<String[]> rawMoveList = new ArrayList<>();
    private static Game game = new Game();
    
    public static List<String[]> getMoveList() {
    	return rawMoveList;
    }
    
    //new game: no moves yet and white starts
    public static void reset() {
    	rawMoveList.clear();
    	player = Color.WHITE;
    	dragStartPos = null;
    }
    
    public static void makeDraggable(ImageView pieceView, BoardPane boardPane) {
        
        pieceView.setOnMousePressed(event -> {
            StackPane square = (StackPane) pieceView.getParent();
            pressPiece(boardPane, boardPane.getPositionFromSquare(square));
        });
        
        pieceView.setOnDragDetected(event -> {
            Dragboard db = pieceView.startDragAndDrop(TransferMode.MOVE);
            
            StackPane square = (StackPane) pieceView.getParent();
            Position startPos = boardPane.getPositionFromSquare(square);
            dragStartPos = startPos;
            ClipboardContent content = new ClipboardContent();
            content.putImage(pieceView.getImage());
            db.setContent(content);

            Image dragImage = pieceView.snapshot(null, null);
            db.setDragView(dragImage, dragImage.getWidth() / 2, dragImage.getHeight() / 2);

            Platform.runLater(() -> pieceView.setVisible(false));
            event.consume();
        });

        pieceView.setOnDragDone(event -> {
        	boardPane.setPiecesAccordingToSettings();
            boardPane.clearBoardColors();
    	    if (!rawMoveList.isEmpty()) {
        	    String[] lastMove = rawMoveList.get(rawMoveList.size() - 1);
                boardPane.highlightMove(lastMove, "#ffe375");
    	    }
            event.consume();
        });
    }

    //shows where the pressed piece can go
    public static void pressPiece(BoardPane boardPane, Position startPos) {
    	if (boardPane.isInputBlocked()) return;
    	if (rawMoveList.isEmpty() && MainUI.settings.isTimerEnabled()) MainUI.whiteTimer.startTimer();
        boardPane.clearBoardColors();
        if (!rawMoveList.isEmpty()) {
    	    String[] lastMove = rawMoveList.get(rawMoveList.size() - 1);
            boardPane.highlightMove(lastMove, "#ffe375");
	    }
        Piece piece = boardPane.getBoard().getPiece(startPos);
        
        if (piece != null && piece.getColor() == player) {
            boardPane.highlightValidSquares(startPos, player, rawMoveList); // Pawn finds the last move for en passent by itself now
        }
    }

    public static void makeDroppable(StackPane square) {
        square.setOnDragOver(event -> {
            if (event.getGestureSource() != square && event.getDragboard().hasImage()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        square.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            Position targetPos = MainUI.boardPane.getPositionFromSquare(square);

            ImageView draggedPiece = (ImageView) event.getGestureSource();

            if (db.hasImage() && dragStartPos != null) {
                boolean moved = dropPiece(dragStartPos, targetPos);
                if (draggedPiece != null) draggedPiece.setVisible(true); // restore piece after move
                event.setDropCompleted(moved);
            } else {
                if (draggedPiece != null) draggedPiece.setVisible(true); // restore piece if no image
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }
    
    //tries to play the move, returns true if it was legal
    public static boolean dropPiece(Position startPos, Position targetPos) {
    	BoardPane boardPane = MainUI.boardPane;
    	if (boardPane.isInputBlocked()) return false; // game over, promotion window or review (redrawing here would show the real board in the review)
    	boolean sameSquare = startPos.getRows() == targetPos.getRows() && startPos.getColumms() == targetPos.getColumms();
    	
    	MoveType type = MoveType.INVALID;
    	if (!sameSquare) type = game.isMoveValid(startPos, targetPos, player, boardPane.getBoard(), rawMoveList);
    	
        if (type == MoveType.INVALID) {
            boardPane.setPiecesAccordingToSettings(); // restore piece
            // nothing was added to rawMoveList, so nothing is removed here (removing deleted the last real move before)
            if (!sameSquare) SoundManager.playIllegal();
            return false;
        }
        
        //Move also sends the position to the engine and lets the bot answer
        player = boardPane.Move(player, rawMoveList, startPos, targetPos);
        return true;
    }
}
