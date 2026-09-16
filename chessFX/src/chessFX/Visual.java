package chessFX;

import chess.*;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class Visual {
	
    public final static String SELECTED_COLOR = "#ffcc00";
    public final static String VALID_LIGHT_COLOR = "#d4b2b1";
    public final static String VALID_DARK_COLOR = "#76514a";
    public final static String LIGHT_COLOR = "#b1d4b6";
    public final static String DARK_COLOR = "#4a7667";
    public final static Font THALEAH = Font.loadFont(Visual.class.getResourceAsStream("/fonts/ThaleahFat.ttf"), 26);
	
	public static void setLabel (Label label, Font font, Color color, int width, int height, Pos aligenment) { 
    	label.setFont(font); 
    	label.setTextFill(color); 
    	label.setMinSize(height, width); 
    	label.setAlignment(aligenment); 
    }
	
	public static void setLabel (Label label, Font font, Color color, int fontSize) { 
		label.setFont(Font.font(font.getName(), fontSize));
    	label.setTextFill(color); 
    }
	
	public static ImageView createPieceView(Piece piece, double size) {
        String symbol = piece.getSymbol();
        String path = "/pieces/" + symbol + ".png"; // assumes correct naming
        Image img = new Image(Visual.class.getResourceAsStream(path), size, size, true, true);
        ImageView imageView = new ImageView(img);
        return imageView;
    }
	
	public static void createShadow(Node node) {
    	DropShadow shadow = new DropShadow();
    	shadow.setRadius(20);
    	shadow.setOffsetX(5);
    	shadow.setOffsetY(5);
    	shadow.setColor(Color.color(0, 0, 0, 0.8));

    	node.setEffect(shadow);   // put the effect on your board
    }
	
	public static ImageView getBackground(String path) {
        Image bgImage = new Image(Visual.class.getResourceAsStream(path));
        ImageView bgView = new ImageView(bgImage);
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);
        bgView.setCache(true);
        return bgView;
    }
	
	public static VBox center(Node node) {
		// HBox centers horizontally
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(node);

        // VBox centers vertically
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(hbox);
        
        return vbox;
	}
	
	public static void setBorders(Node node, double size) {
         node.setStyle(node.getStyle() + "-fx-border-color: black; -fx-border-style: solid; -fx-border-width: " + size);
	}
	
	public static void pickedAnimation(Node node, double size) {
		
		ScaleTransition stEnter = new ScaleTransition(Duration.millis(150), node);
        stEnter.setToX(size);
        stEnter.setToY(size);

        ScaleTransition stExit = new ScaleTransition(Duration.millis(150), node);
        stExit.setToX(1);
        stExit.setToY(1);

        node.setOnMouseEntered(e -> stEnter.playFromStart());
        node.setOnMouseExited(e -> stExit.playFromStart());
	}
	
    public static void fadeIn(StackPane overlay) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), overlay);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
	
	public static void addHoverTranslate(Node node, double offset, double durationMillis) {
	    TranslateTransition enter = new TranslateTransition(Duration.millis(durationMillis), node);
	    enter.setToY(-offset);

	    TranslateTransition exit = new TranslateTransition(Duration.millis(durationMillis), node);
	    exit.setToY(0);

	    node.setOnMouseEntered(e -> enter.playFromStart());
	    node.setOnMouseExited(e -> exit.playFromStart());
	}
	
	public static void updateTurnLabel(chess.Color player) {
	    String text = (player == chess.Color.WHITE) ? "White's Turn" : "Black's Turn";
	    MainUI.turnLabel.setText(text);
	}
	
	public static void scaleAnimation(Node node) {
		node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), node);
            st.setToX(1.1);
            st.setToY(1.1);
            st.play();
        });
		node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
	}
}
