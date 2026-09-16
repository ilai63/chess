package chessFX;

import chess.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class CapturedPanel {

    private final Color color;
    private final VBox container = new VBox();
    private final ScrollPane scrollPane = new ScrollPane();
    private final VBox pieceBox = new VBox();
    private final Label materialLabel = new Label();
    private int materialCounter = 0;

    private final Font THALEAH = Font.loadFont(getClass().getResourceAsStream("/fonts/ThaleahFat.ttf"), 26);

    public CapturedPanel(Color color) {
        this.color = color;
        configure();
    }

    private void configure() {
        // Label styling
        Visual.setLabel(materialLabel, THALEAH, javafx.scene.paint.Color.WHITE, 60, 40, Pos.CENTER);

        // Piece box
        pieceBox.setAlignment(Pos.TOP_CENTER);
        pieceBox.setStyle("-fx-background-color: transparent;");

        // Scroll pane setup
        scrollPane.setContent(pieceBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(60);
        scrollPane.setMaxHeight(480);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-control-inner-background: transparent;");
        scrollPane.getStyleClass().add("transparent-scrollpane");
        SmoothScroll.apply(scrollPane);

        // Container setup
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(5);
        container.setStyle("-fx-background-color: transparent;");
        container.getChildren().addAll(materialLabel, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    public VBox getContainer() {
        return container;
    }

    public Color getColor() {
        return color;
    }

    public int getCount() {
        return materialCounter;
    }

    public int materialAdvantage(CapturedPanel opposite) {
        return getCount() - opposite.getCount();
    }

    public void addCapturedPiece(Piece piece, CapturedPanel opposite) {
        ImageView pieceImage = Visual.createPieceView(piece, 58.6);
        pieceBox.getChildren().add(pieceImage);
        materialCounter += getPieceValue(piece);
        updateMaterialLabel(opposite);
        opposite.updateMaterialLabel(this);
    }

    private int getPieceValue(Piece piece) {
        switch (piece.getSubClass()) {
            case "Pawn": return 1;
            case "Knight": return 3;
            case "Bishop": return 3;
            case "Rook": return 5;
            case "Queen": return 9;
            default: return 0;
        }
    }

    public void reset() {
        pieceBox.getChildren().clear();
        materialCounter = 0;
        materialLabel.setText("");
    }

    public void updateMaterialLabel(CapturedPanel opposite) {
        int diff = materialAdvantage(opposite);
        if (diff > 0)
            materialLabel.setText("+" + diff);
        else
            materialLabel.setText("");
    }
}