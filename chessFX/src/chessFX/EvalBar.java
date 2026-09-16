package chessFX;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class EvalBar extends StackPane {

    private Rectangle blackOverlay;
    private double barWidth;
    private double currentValue = 0.5;
    private Label evalLabel;

    public EvalBar(double width, double height) {
        barWidth = width;
        Rectangle whiteBackground = new Rectangle(width, height);
        whiteBackground.setFill(Color.web(Visual.LIGHT_COLOR));

        evalLabel = new Label("");
        Visual.setLabel(evalLabel, Visual.THALEAH, Color.BLACK, 20);

        // Black overlay (adjust width dynamically)
        blackOverlay = new Rectangle(width * 0.5, height);
        blackOverlay.setFill(Color.web(Visual.DARK_COLOR));
        StackPane.setAlignment(blackOverlay, Pos.BOTTOM_LEFT);

        this.getChildren().addAll(whiteBackground, blackOverlay, evalLabel);

        // Center vertically, move horizontally with bar
        setAlignment(evalLabel, Pos.CENTER);
    }

    public void setLabel(String text) {
        evalLabel.setText(text);
    }

    public void update(double eval, String mateIn) {
        // mate = moves to mate (negative when black mates), the api sends null when there is no mate
        int mateMoves = 0;
        if (mateIn != null) {
        	try {
        		mateMoves = Integer.parseInt(mateIn.replaceAll("\"", "").trim());
        	} catch (NumberFormatException e) {
        		mateMoves = 0;
        	}
        }
        if (mateMoves != 0) eval = mateMoves > 0 ? 100.0 : -100.0;
        
        // Text update
        if (eval < 100.0 && eval > -100.0) {
            evalLabel.setText(String.format("%.2f", eval));
            StackPane.setMargin(evalLabel, new Insets(0, 0, 0, 0));
        } else if (eval >= 100.0) {
            setLabel(mateMoves != 0 ? "mate in " + Math.abs(mateMoves) : "mate");
            StackPane.setMargin(evalLabel, new Insets(0, 0, 0, 100));
        } else {
        	setLabel(mateMoves != 0 ? "mate in " + Math.abs(mateMoves) : "mate");
            StackPane.setMargin(evalLabel, new Insets(0, 100, 0, 0));
        }

        // Calculate normalized position
        double targetValue = normalizeEval(eval);
        double startWidth = barWidth * (1 - currentValue);
        double targetWidth = barWidth * (1 - targetValue);
        double diff = Math.abs(targetValue - currentValue);
        double duration = 150 + diff * 150;

        // Animate both bar width and label position
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(blackOverlay.widthProperty(), startWidth),
                new KeyValue(evalLabel.translateXProperty(), startWidth - barWidth / 2)
            ),
            new KeyFrame(Duration.millis(duration),
                new KeyValue(blackOverlay.widthProperty(), targetWidth, Interpolator.EASE_BOTH),
                new KeyValue(evalLabel.translateXProperty(), targetWidth - barWidth / 2, Interpolator.EASE_BOTH)
            )
        );
        timeline.play();

        currentValue = targetValue;
    }

    private double normalizeEval(double eval) {
        if (eval >= 100.0) return 1.0;    // White mates
        if (eval <= -100.0) return 0.0;   // Black mates

        double clamped = Math.max(-10, Math.min(10, eval));
        return (clamped + 10) / 20.0;
    }
}
