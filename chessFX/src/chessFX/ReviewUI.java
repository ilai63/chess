package chessFX;

import java.util.List;
import chess.Board;
import chess.Color;
import chess.Piece;
import engine.EngineResponse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ReviewUI {
	
	private Button forward;
	private Button backward;
	private Button leave;
	private VBox vboxTop;
	private VBox vboxBottom;
	private int moveNum = 0;
	private EvalBar evalBar;
	private List<String[]> rawMoveList;
	
	public ReviewUI (List<String[]> rawMoveList) {
		this.rawMoveList = rawMoveList;
	}
	
	public void setup(Color color) {
		MainUI.boardPane.setReviewing(true);
    	MainUI.checkLabel.setText("");
    	MainUI.turnLabel.setText("");
    	
    	setUpButtons(color);
    	
    	HBox buttonBox = new HBox(5);
    	buttonBox.setPadding(new Insets(20, 0, 10, 0));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(backward, leave, forward);
        vboxTop = new VBox(70, buttonBox);
        
        evalBar = new EvalBar(840, 20);
        
        vboxBottom = new VBox(evalBar);
        vboxBottom.setAlignment(Pos.BOTTOM_CENTER);
        
        MainUI.rootPane.getChildren().addAll(vboxBottom, vboxTop);
        
        moveNum = 0;
        showPosition();
	}
	
	public void setUpButtons(Color color) {
		
		forward = new Button();
		backward = new Button();
		leave = new Button();
		
		ImageView forwardIcon = new ImageView("/buttons/Rarrow.png");
		setButton(forward, forwardIcon);
		forward.setOnAction(e -> {
        	moveForwards();
        });
		
		ImageView backwardIcon = new ImageView("/buttons/Larrow.png");
		setButton(backward, backwardIcon);
		backward.setOnAction(e -> {
        	moveBackwards();
        });
		
		ImageView exitIcon = new ImageView("/buttons/exit.png");
		setButton(leave, exitIcon);
		leave.setOnAction(e -> {
        	returnToEnd(color);
        });
	}
	
	public void setButton(Button button, ImageView icon) {
		
		icon.setFitWidth(60);
		icon.setFitHeight(60);
		icon.setPreserveRatio(true);
		button.setGraphic(icon);
		button.setStyle("-fx-background-color: transparent;");
		button.setAlignment(Pos.CENTER);
		Visual.scaleAnimation(button);
	}
	
	public void moveForwards() {
	    if (moveNum >= lastPosition()) return; // bounds check
	    moveNum++;
	    showPosition();
	}
	
	public void moveBackwards() {
	    if (moveNum <= 0) return; // can't go before the first move
	    moveNum--;
	    showPosition();
	}
	
	//index of the last saved position (the same as the number of moves)
	private int lastPosition() {
		return Math.min(rawMoveList.size(), MainUI.boardPane.getPositionCount() - 1);
	}
	
	//shows the board after moveNum moves (the positions are saved by BoardPane during the game)
	private void showPosition() {
		MainUI.boardPane.clearBoardColors();
		MainUI.boardPane.drawPieces(MainUI.boardPane.getPosition(moveNum), "none");
		if (moveNum >= 1) MainUI.boardPane.highlightMove(rawMoveList.get(moveNum - 1), "#ffe375");
		showCapturedPieces(moveNum);
		
		EngineResponse eva = MainUI.boardPane.getEvaluation(moveNum);
		if (moveNum == lastPosition() && !MainUI.boardPane.getEndText().isEmpty()) {
			evalBar.setLabel(MainUI.boardPane.getEndText()); // the last position: "Mate", "Draw" or "Time"
		} else if (eva != null) {
			evalBar.update(eva.eval, eva.mate);
		} else {
			evalBar.setLabel(""); // the engine didn't answer for this position (no internet)
		}
	}
	
	//the captured panels show only what was taken in the first "moves" moves
	private void showCapturedPieces(int moves) {
		MainUI.whiteCapturedBox.reset();
		MainUI.blackCapturedBox.reset();
		for (int i = 0; i < moves; i++) {
			Board before = MainUI.boardPane.getPosition(i);
			int from = Integer.parseInt(rawMoveList.get(i)[1]);
			int to = Integer.parseInt(rawMoveList.get(i)[2]);
			Piece captured = before.getPiece(to / 10, to % 10);
			//en passent: the pawn moved sideways to an empty square, the taken pawn is next to where it started
			if (captured == null && rawMoveList.get(i)[0].equals("Pawn") && from % 10 != to % 10) captured = before.getPiece(from / 10, to % 10);
			if (captured == null) continue;
			
			if (captured.getColor() == Color.WHITE) MainUI.whiteCapturedBox.addCapturedPiece(captured, MainUI.blackCapturedBox);
			else MainUI.blackCapturedBox.addCapturedPiece(captured, MainUI.whiteCapturedBox);
		}
	}
	
	public void returnToEnd(Color color) {
		MainUI.boardPane.setReviewing(false);
		MainUI.boardPane.clearBoardColors();
		MainUI.boardPane.setPiecesAccordingToSettings(); // the real board never changed, it's still the final position
		if (!rawMoveList.isEmpty()) MainUI.boardPane.highlightMove(rawMoveList.get(rawMoveList.size() - 1), "#ffe375");
		showCapturedPieces(lastPosition()); // everything that was captured in the game again
		MainUI.rootPane.getChildren().removeAll(vboxTop, vboxBottom);
		EndingUI.show(color, rawMoveList);
	}
}
