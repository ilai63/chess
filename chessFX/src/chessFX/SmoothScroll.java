package chessFX;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

public class SmoothScroll {

    public static void apply(ScrollPane scrollPane) {
        scrollPane.setOnScroll(event -> {
            double delta = event.getDeltaY() / 1200;
            double targetVValue = clamp(scrollPane.getVvalue() - delta, 0, 1);

            Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200),
                    new KeyValue(scrollPane.vvalueProperty(), targetVValue))
            );
            timeline.play();

            event.consume();
        });
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}