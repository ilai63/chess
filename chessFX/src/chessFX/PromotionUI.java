package chessFX;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import chess.*;

public class PromotionUI {
	
	public interface PromotionHandler {
        void onPieceChosen(Piece piece);
    }

	private static StackPane currentOverlay; //the open promotion window, null if there is none

	//removes the window without choosing (the game ended or restarted)
	public static void close() {
		if (currentOverlay != null) {
			MainUI.rootPane.getChildren().remove(currentOverlay);
			currentOverlay = null;
		}
	}

    public static void show(Color pieceColor, PromotionHandler handler) {
        StackPane overlay = new StackPane();
        currentOverlay = overlay;
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.setPrefSize(MainUI.rootPane.getWidth(), MainUI.rootPane.getHeight());

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        buttonBox.getChildren().addAll(
                createPieceButton("Queen", pieceColor, handler, overlay),
                createPieceButton("Rook", pieceColor, handler, overlay),
                createPieceButton("Bishop", pieceColor, handler, overlay),
                createPieceButton("Knight", pieceColor, handler, overlay)
        );

        overlay.getChildren().add(buttonBox);
        StackPane.setAlignment(buttonBox, Pos.CENTER);

        // Add overlay with fade-in effect
        MainUI.rootPane.getChildren().add(overlay);
        Visual.fadeIn(overlay);
    }

    private static Button createPieceButton(String pieceType, Color pieceColor, PromotionHandler handler, StackPane overlay) {
        String prefix = pieceColor == Color.WHITE ? "W" : "B"; // the files are WQ.png, BQ.png... and inside a jar the name is case sensitive
        String symbol = prefix + getPieceSymbol(pieceType);

        ImageView imageView = new ImageView(new Image(PromotionUI.class.getResourceAsStream("/pieces/" + symbol + ".png"), 80, 80, true, true));
        Button button = new Button("", imageView);
        button.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        button.setOnMouseEntered(e -> {
            button.setScaleX(1.1);
            button.setScaleY(1.1);
        });
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });

        button.setOnAction(e -> {
            // only the first click counts (a double click would promote twice)
            for (javafx.scene.Node node : ((HBox) button.getParent()).getChildren()) node.setDisable(true);
            Piece chosenPiece = createPiece(pieceType, pieceColor);
            fadeOut(overlay, () -> {
                if (currentOverlay != overlay) return; //closed while fading out
                currentOverlay = null;
                MainUI.rootPane.getChildren().remove(overlay);
                handler.onPieceChosen(chosenPiece);
            });
        });

        return button;
    }

    private static Piece createPiece(String type, Color color) {
        switch (type) {
            case "Queen": return new Queen(color);
            case "Rook": return new Rook(color);
            case "Bishop": return new Bishop(color);
            case "Knight": return new Knight(color);
            default: return new Queen(color);
        }
    }

    private static String getPieceSymbol(String type) {
        switch (type) {
            case "Queen": return "Q";
            case "Rook": return "R";
            case "Bishop": return "B";
            case "Knight": return "N";
            default: return "Q";
        }
    }

    private static void fadeOut(StackPane overlay, Runnable afterFade) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), overlay);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> afterFade.run());
        ft.play();
    }
}
