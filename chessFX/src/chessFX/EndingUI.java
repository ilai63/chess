package chessFX;

import java.util.List;

import chess.*;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class EndingUI {


    public static void show(Color player, List<String[]> rawMoveList) {
        MainUI.boardPane.endGame();
        // the clocks stop, otherwise the running one reaches 0 later and ends the game a second time
        if (MainUI.settings.isTimerEnabled()) {
        	MainUI.whiteTimer.pauseTimer();
        	MainUI.blackTimer.pauseTimer();
        }
        StackPane overlay = new StackPane();
        MainUI.boardPane.clearBoardColors();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.setPrefSize(MainUI.rootPane.getWidth(), MainUI.rootPane.getHeight());
        Color winner = null;
        if (player != null) winner = player.switchColors();
        Label label;
        if (winner == Color.WHITE) {
            label = new Label("White wins");
        } else if (winner == Color.BLACK) {
            label = new Label("Black wins");
        } else {
            label = new Label("Draw");
        }
        
        label.setAlignment(Pos.CENTER);
        Visual.setLabel(label, Visual.THALEAH, javafx.scene.paint.Color.WHITE, 100);
        
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(createResetButton(overlay, rawMoveList), createReviewButton(overlay, rawMoveList, player));
        
        Button backToMainMenu = createMainMenuButton(overlay);
        VBox vbox = new VBox();
        vbox.getChildren().addAll(label, buttonBox, backToMainMenu);

        VBox.setMargin(label, new Insets(0, 0, 70, 0)); // bottom margin 50px
        VBox.setMargin(buttonBox, new Insets(0, 0, 5, 0));
        vbox.setAlignment(Pos.CENTER);
        vbox.setSnapToPixel(false);
        
        
        overlay.getChildren().add(vbox);

        MainUI.rootPane.getChildren().add(overlay);

        Visual.fadeIn(overlay);
    }
    
    private static Button createResetButton(StackPane overlay, List<String[]> rawMoveList) {
		Button button = new Button("Play Again");
		button.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: white;");
        button.setFont(Font.font(Visual.THALEAH.getName(), 40));
        Visual.scaleAnimation(button);
        
        button.setOnAction(e -> {
        	if (!firstClick(overlay)) return;
        	MainUI.whiteCapturedBox.reset();
        	MainUI.blackCapturedBox.reset();
        	MainUI.turnLabel.setText("White's Turn");
        	MainUI.checkLabel.setText("");
        	if (MainUI.settings.isTimerEnabled()) { // without a timer these are null
        		MainUI.blackTimer.resetTimer(MainUI.settings.getTimerTime());
        		MainUI.whiteTimer.resetTimer(MainUI.settings.getTimerTime());
        	}
        	// new board, empty move list, white to move, and the bot starts if it plays white
        	MainUI.boardPane.setup();
            fadeOut(overlay, () -> {
            	button.setScaleX(1.0);
                button.setScaleY(1.0);
                MainUI.rootPane.getChildren().remove(overlay);
            });
        });
        
        return button;
    }
    
    private static Button createReviewButton(StackPane overlay, List<String[]> rawMoveList, Color player) {
    	Button button = new Button("Review");
    	button.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: white;");
        button.setFont(Font.font(Visual.THALEAH.getName(), 40));
        Visual.scaleAnimation(button);
        
        button.setOnAction(e -> {
        	if (!firstClick(overlay)) return;
            // the captured panels follow the review position (ReviewUI updates them)
            fadeOut(overlay, () -> {
            	button.setScaleX(1.0);
                button.setScaleY(1.0);
                MainUI.rootPane.getChildren().remove(overlay);
                ReviewUI review = new ReviewUI(rawMoveList);
                review.setup(player);
            });
        });
        
    	return button;
    }
    
    private static void fadeOut(StackPane overlay, Runnable afterFade) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), overlay);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> afterFade.run());
        ft.play();
    }
    
    // only the first click on the ending screen counts, a double click on Review opened the review twice
    private static boolean firstClick(StackPane overlay) {
        return overlay.getProperties().putIfAbsent("clicked", true) == null;
    }
    
    private static Button createMainMenuButton(StackPane overlay) {
        Button button = new Button("Main Menu");
        button.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: white;");
        button.setFont(Font.font(Visual.THALEAH.getName(), 40));
        Visual.scaleAnimation(button);

        button.setOnAction(e -> {
        	if (!firstClick(overlay)) return;
            // Animate button on start
            FadeTransition fade = new FadeTransition(Duration.millis(500), button);
            fade.setToValue(0); // fade out
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(500), button);
            scale.setToX(0.5); // shrink
            scale.setToY(0.5);
            
            ParallelTransition anim = new ParallelTransition(fade, scale);
            anim.setOnFinished(ev -> {
                // after animation ends, switch scene
                Stage stage = (Stage) button.getScene().getWindow();
                stage.setScene(LoadScreenUI.getUI());
            });
            anim.play();
        });

        return button;
    }
}
