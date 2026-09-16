package chessFX;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class SettingsUI {
    public static GameSettings settings = new GameSettings(false, false, null, 300); // 5 minutes like the slider shows

    public static void show(StackPane rootPane) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        overlay.setPrefSize(rootPane.getWidth(), rootPane.getHeight());

        Label settingsLabel = new Label("Settings");
        Visual.setLabel(settingsLabel, Visual.THALEAH, Color.WHITE, 100);

        Label timer = new Label("Timer Settings");
        Visual.setLabel(timer, Visual.THALEAH, Color.WHITE, 50);
        HBox timerBox = createTimerBox();

        Label engine = new Label("Engine Settings");
        Visual.setLabel(engine, Visual.THALEAH, Color.WHITE, 50);
        VBox engineBox = createEngineBox();

        Button returnButton = createReturnButton(overlay, rootPane);

        VBox layout = new VBox(20, settingsLabel, timer, timerBox, engine, engineBox, returnButton);
        layout.setAlignment(Pos.TOP_CENTER);
        overlay.getChildren().add(layout);

        rootPane.getChildren().add(overlay);

        Visual.fadeIn(overlay);
    }

    public static HBox createTimerBox() {
        VBox timerBox = new VBox(5);
        timerBox.setAlignment(Pos.CENTER);

        // Label showing if timer is enabled/disabled
        Label timerLabel = new Label();
        Visual.setLabel(timerLabel, Visual.THALEAH, Color.WHITE, 18);

        // Toggle button to enable/disable timer
        ToggleButton timerCheck = new ToggleButton();
        Visual.scaleAnimation(timerCheck);
        timerCheck.setPrefSize(60, 60);
        timerCheck.setStyle("""
            -fx-background-radius: 0;
            -fx-background-color: #b1d4b6;
            -fx-border-color: black;
            -fx-border-width: 6;
            -fx-background-insets: 0;
            -fx-border-insets: 0;
        """);

        // Slider for timer (in seconds)
        Slider timeSlider = new Slider(60, 3600, 300);
        timeSlider.setShowTickMarks(false);
        timeSlider.setShowTickLabels(false);
        timeSlider.setMajorTickUnit(60); // snaps to whole minutes
        timeSlider.setMinorTickCount(0);
        timeSlider.setBlockIncrement(60);
        timeSlider.setSnapToTicks(true);
        timeSlider.setPrefWidth(360); // adjust width
        timeSlider.getStylesheets().add(SettingsUI.class.getResource("/styles/styles.css").toExternalForm());

        // Label above slider showing minutes
        Label minutesLabel = new Label();
        Visual.setLabel(minutesLabel, Visual.THALEAH, Color.WHITE, 20);
        int initialMinutes = (int) timeSlider.getValue() / 60;
        minutesLabel.setText(minutesText(initialMinutes));

        // Update label and settings when slider moves
        timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int minutes = newVal.intValue() / 60;
            minutesLabel.setText(minutesText(minutes));
            settings.setTimerTime(newVal.intValue());
        });

        // VBox to hold slider + minutes label
        VBox sliderBox = new VBox(5, minutesLabel, timeSlider);
        sliderBox.setAlignment(Pos.CENTER);

        // Add timer label + toggle button
        timerBox.getChildren().addAll(timerLabel, timerCheck);

        HBox hbox = new HBox(timerBox);
        hbox.setAlignment(Pos.CENTER);

        // --- Initialize from settings ---
        timerCheck.setSelected(settings.isTimerEnabled());
        if (settings.isTimerEnabled()) {
            timerLabel.setText("Timer Enabled");
            if (!hbox.getChildren().contains(sliderBox)) hbox.getChildren().add(sliderBox);
            timerCheck.setStyle(timerCheck.getStyle().replace("#b1d4b6", "#4a7667"));
        } else {
            timerLabel.setText("Timer Disabled");
        }
        timeSlider.setValue(settings.getTimerTime());

        // Toggle behavior
        timerCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                timerLabel.setText("Timer Enabled");
                if (!hbox.getChildren().contains(sliderBox)) hbox.getChildren().add(sliderBox);
                settings.setTimerSetting(true);
                timerCheck.setStyle(timerCheck.getStyle().replace("#b1d4b6", "#4a7667"));
            } else {
                timerLabel.setText("Timer Disabled");
                hbox.getChildren().remove(sliderBox);
                settings.setTimerSetting(false);
                timerCheck.setStyle(timerCheck.getStyle().replace("#4a7667", "#b1d4b6"));
            }
        });

        return hbox;
    }

    private static String minutesText(int minutes) {
        return minutes == 1 ? "1 minute" : minutes + " minutes";
    }

    public static VBox createEngineBox() {
        VBox stockfishBox = new VBox(10);
        stockfishBox.setAlignment(Pos.CENTER);

        HBox stockfishTurn = createTurnBox();

        Label stockfishLabel = new Label();
        Visual.setLabel(stockfishLabel, Visual.THALEAH, Color.WHITE, 18);

        ToggleButton stockfishCheck = new ToggleButton();
        Visual.scaleAnimation(stockfishCheck);
        stockfishCheck.setPrefSize(60, 60);
        stockfishCheck.setStyle("""
            -fx-background-radius: 0;
            -fx-background-color: #b1d4b6;
            -fx-border-color: black;
            -fx-border-width: 6;
            -fx-background-insets: 0;
            -fx-border-insets: 0;
        """);

        stockfishBox.getChildren().addAll(stockfishLabel, stockfishCheck);

        // --- Initialize from settings ---
        stockfishCheck.setSelected(settings.isBotEnabled());
        if (settings.isBotEnabled()) {
            stockfishLabel.setText("One Player Game");
            if (!stockfishBox.getChildren().contains(stockfishTurn)) {
                stockfishBox.getChildren().add(stockfishTurn);
            }
            stockfishCheck.setStyle(stockfishCheck.getStyle().replace("#b1d4b6", "#4a7667"));
        } else {
            stockfishLabel.setText("Two Player Game");
        }

        // Handle toggle behavior
        stockfishCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                stockfishLabel.setText("One Player Game");
                if (!stockfishBox.getChildren().contains(stockfishTurn)) stockfishBox.getChildren().add(stockfishTurn);
                settings.setBotSetting(true);
                stockfishCheck.setStyle(stockfishCheck.getStyle().replace("#b1d4b6", "#4a7667"));
            } else {
                stockfishLabel.setText("Two Player Game");
                stockfishBox.getChildren().remove(stockfishTurn);
                settings.setBotSetting(false);
                stockfishCheck.setStyle(stockfishCheck.getStyle().replace("#4a7667", "#b1d4b6"));
            }
        });

        return stockfishBox;
    }

    public static HBox createTurnBox() {
        HBox stockfishTurn = new HBox(10);
        stockfishTurn.setAlignment(Pos.CENTER);

        ToggleGroup turnGroup = new ToggleGroup();

        ToggleButton white = new ToggleButton();
        Visual.scaleAnimation(white);
        white.setPrefSize(40, 40);
        white.setStyle("""
            -fx-background-radius: 0;
            -fx-background-color: white;
            -fx-border-color: #b1d4b6;
            -fx-border-width: 6;
            -fx-background-insets: 0;
            -fx-border-insets: 0;
        """);
        white.setToggleGroup(turnGroup);

        ToggleButton random = new ToggleButton();
        Visual.scaleAnimation(random);
        random.setPrefSize(40, 40);
        random.setStyle("""
            -fx-background-radius: 0;
            -fx-background-color: grey;
            -fx-border-color: #b1d4b6;
            -fx-border-width: 6;
            -fx-background-insets: 0;
            -fx-border-insets: 0;
        """);
        random.setToggleGroup(turnGroup);

        ToggleButton black = new ToggleButton();
        Visual.scaleAnimation(black);
        black.setPrefSize(40, 40);
        black.setStyle("""
            -fx-background-radius: 0;
            -fx-background-color: black;
            -fx-border-color: #b1d4b6;
            -fx-border-width: 6;
            -fx-background-insets: 0;
            -fx-border-insets: 0;
        """);
        black.setToggleGroup(turnGroup);

        // --- Initialize selection ---
        if (settings.getBotTurn() == chess.Color.WHITE) white.setSelected(true);
        else if (settings.getBotTurn() == chess.Color.BLACK) black.setSelected(true);
        else random.setSelected(true);

        // Apply visual highlight for the initially selected button
        ToggleButton selectedButton = (ToggleButton) turnGroup.getSelectedToggle();
        if (selectedButton != null) {
            selectedButton.setStyle(selectedButton.getStyle().replace("#b1d4b6", "#4a7667"));
        }

        // Listen to selection changes
        turnGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            white.setStyle(white.getStyle().replace("#4a7667", "#b1d4b6"));
            random.setStyle(random.getStyle().replace("#4a7667", "#b1d4b6"));
            black.setStyle(black.getStyle().replace("#4a7667", "#b1d4b6"));

            if (newToggle != null) {
                ToggleButton selected = (ToggleButton) newToggle;
                selected.setStyle(selected.getStyle().replace("#b1d4b6", "#4a7667"));

                if (selected == white) settings.setBotTurn(chess.Color.WHITE);
                else if (selected == black) settings.setBotTurn(chess.Color.BLACK);
                else settings.setBotTurn(null);
            } else {
                settings.setBotTurn(null);
            }
        });

        stockfishTurn.getChildren().addAll(white, random, black);
        return stockfishTurn;
    }

    public static Button createReturnButton(StackPane overlay, StackPane rootPane) {
        Button button = new Button("Return");
        button.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: white;");
        button.setFont(Font.font(Visual.THALEAH.getName(), 60));
        button.setAlignment(Pos.CENTER);
        Visual.scaleAnimation(button);

        button.setOnAction(e -> {
            fadeOut(overlay, () -> {
                button.setScaleX(1.0);
                button.setScaleY(1.0);
                rootPane.getChildren().remove(overlay);
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
}
