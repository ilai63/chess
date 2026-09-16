package chessFX;

import chess.*;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoadScreenUI {
	private static StackPane rootPane;
	
	public static Scene getUI() {
	    // Background
	    ImageView bgView = Visual.getBackground("/backgrounds/cityBackground.png");

	    // Top VBox: headline + piece box
	    Label headline = new Label("Chess");
	    Visual.setLabel(headline, Visual.THALEAH, Color.WHITE, 150);

	    StackPane pieceBox = createPieceBox();

	    VBox topBox = new VBox(20, headline, pieceBox);
	    topBox.setAlignment(Pos.CENTER);
	    topBox.setTranslateY(-100);
	    // Bottom VBox: buttons
	    Button start = createStartButton();
	    Button settings = createSettingsButton();
	    Button exit = createExitButton();

	    VBox bottomBox = new VBox();
	    bottomBox.setAlignment(Pos.CENTER);
	    bottomBox.setTranslateY(170);

	    bottomBox.getChildren().addAll(start, settings, exit);

	    // Root StackPane with background
	    rootPane = new StackPane(bgView, topBox, bottomBox);

	    Scene scene = new Scene(rootPane, 840, 660);
	    return scene;
	}


	
	private static StackPane createPieceBox() {
	    StackPane stack = new StackPane();

	    double offsetx = 70; // controls spacing between layers
	    double offsety = -10;
	    
	    King king = new King(chess.Color.WHITE);
	    ImageView kingView = Visual.createPieceView(king, 100);
	    kingView.setViewOrder(0);
	    
	    Bishop bishop1 = new Bishop(chess.Color.WHITE);
	    ImageView bishop1View = Visual.createPieceView(bishop1, 100);
	    bishop1View.setViewOrder(1);
	    bishop1View.setTranslateX(offsetx);
	    bishop1View.setTranslateY(offsety);
	    
	    Bishop bishop2 = new Bishop(chess.Color.WHITE);
	    ImageView bishop2View = Visual.createPieceView(bishop2, 100);
	    bishop2View.setViewOrder(1);
	    bishop2View.setTranslateX(-offsetx);
	    bishop2View.setTranslateY(offsety);
	    
	    Knight knight1 = new Knight(chess.Color.WHITE);
	    ImageView knight1View = Visual.createPieceView(knight1, 100);
	    knight1View.setViewOrder(2);
	    knight1View.setTranslateX(offsetx * 2);
	    knight1View.setTranslateY(offsety * 2);
	    
	    Knight knight2 = new Knight(chess.Color.WHITE);
	    ImageView knight2View = Visual.createPieceView(knight2, 100);
	    knight2View.setViewOrder(2);
	    knight2View.setTranslateX(-offsetx * 2);
	    knight2View.setTranslateY(offsety * 2);
	    
	    Rook rook1 = new Rook(chess.Color.WHITE);
	    ImageView rook1View = Visual.createPieceView(rook1, 100);
	    rook1View.setViewOrder(3);
	    rook1View.setTranslateX(offsetx * 3);
	    rook1View.setTranslateY(offsety * 3);
	    
	    Rook rook2 = new Rook(chess.Color.WHITE);
	    ImageView rook2View = Visual.createPieceView(rook2, 100);
	    rook2View.setViewOrder(3);
	    rook2View.setTranslateX(-offsetx * 3);
	    rook2View.setTranslateY(offsety * 3);

	    stack.getChildren().addAll(
	        kingView,
	        bishop1View, bishop2View,
	        knight1View, knight2View,
	        rook1View, rook2View
	    );

	    stack.setAlignment(Pos.CENTER);
	    return stack;
	}
	
	private static Button createStartButton () {
		
		Button start = new Button("Play");
		start.setViewOrder(-1);
		start.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: white;");
        start.setFont(Font.font(Visual.THALEAH.getName(), 60));
        start.setAlignment(Pos.CENTER);
        Visual.scaleAnimation(start);
        
        start.setOnAction(e -> {
            // Animate button on start
            FadeTransition fade = new FadeTransition(Duration.millis(500), start);
            fade.setToValue(0); // fade out
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(500), start);
            scale.setToX(0.5); // shrink
            scale.setToY(0.5);
            
            ParallelTransition anim = new ParallelTransition(fade, scale);
            anim.setOnFinished(ev -> {
                // after animation ends, switch scene
                Stage stage = (Stage) start.getScene().getWindow();
                stage.setScene(MainUI.getUI(SettingsUI.settings));
            });
            anim.play();
        });

		return start;
	}
	
	public static Button createSettingsButton() {
		Button button = new Button("Settings");
		button.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: white;");
        button.setFont(Font.font(Visual.THALEAH.getName(), 60));
        button.setAlignment(Pos.CENTER);
        Visual.scaleAnimation(button);
        
        button.setOnAction(e -> {
            SettingsUI.show(rootPane);
        });
        
        return button;
	}
	
	public static Button createExitButton() {
	    Button quit = new Button("Quit");
	    quit.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: white;");
	    quit.setFont(Font.font(Visual.THALEAH.getName(), 60));
	    quit.setAlignment(Pos.CENTER);
	    Visual.scaleAnimation(quit);

	    quit.setOnAction(e -> {
	        javafx.application.Platform.exit(); // exits the application
	    });

	    return quit;
	}
}

