package chessFX;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class ChessTimer {
    private int remainingSeconds;
    private final Label timerLabel;
    private Timeline timeline;
    private final Runnable timeUpAction;

    public ChessTimer(int initialSeconds, Runnable timeUpAction) {
        this.remainingSeconds = Math.max(0, initialSeconds);
        this.timeUpAction = timeUpAction;

        timerLabel = new Label(formatTime(remainingSeconds));
        timerLabel.setPrefSize(100, 100);
        Visual.setLabel(timerLabel, Visual.THALEAH, Color.WHITE, 30);

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void tick() {
        if (remainingSeconds > 0) {
            remainingSeconds--;
            timerLabel.setText(formatTime(remainingSeconds));
        }
        // the time is up as soon as the clock shows 00:00 (before it waited one more second)
        if (remainingSeconds <= 0) {
            timeline.stop();
            if (timeUpAction != null) timeUpAction.run();
        }
    }

    private String formatTime(int seconds) {
        if (seconds < 0) seconds = 0; // clamp

        int min = seconds / 60;
        int sec = seconds % 60;

        // format both to always have two digits
        return String.format("%02d:%02d", min, sec);
    }

    public void startTimer() {
        if (remainingSeconds > 0) timeline.play();
    }

    public void pauseTimer() {
        timeline.pause();
    }

    public void resumeTimer() {
        if (remainingSeconds > 0) timeline.play();
    }

    public void resetTimer(int newSeconds) {
        pauseTimer();
        this.remainingSeconds = Math.max(0, newSeconds);
        timerLabel.setText(formatTime(remainingSeconds));
    }

    public Label getLabel() {
        return timerLabel;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

	public void onMoveMade(boolean isWhite, ChessTimer other) {
		if (!MainUI.settings.isTimerEnabled()) return;

	    if (isWhite) {
	        other.startTimer(); // or resumeTimer if already started
	        this.pauseTimer();
	    } else {
	        this.startTimer();
	        other.pauseTimer();
	    }
	}
}
