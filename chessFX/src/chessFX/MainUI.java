package chessFX;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MainUI {

    private static final int TILE_SIZE = 60;
    private static final int BOARD_SIZE = 8;

    public static CapturedPanel whiteCapturedBox;
    public static CapturedPanel blackCapturedBox;
    
    public static ChessTimer whiteTimer;
    public static ChessTimer blackTimer;
    
    public static BoardPane boardPane = new BoardPane();
    public static Label turnLabel;
    public static Label checkLabel;
    public static StackPane rootPane;
    public static GameSettings settings;
    public static Scene scene;
    
    public static Scene getUI(GameSettings gameSettings) {
    	
    	// a copy for this game, so a random bot color doesn't overwrite the "random" choice in the settings
    	settings = new GameSettings(gameSettings);
    	if (settings.getBotTurn() == null) settings.setBotTurn(Math.random() < 0.5 ? chess.Color.WHITE : chess.Color.BLACK);
    	
        // Background
        ImageView bgView = Visual.getBackground("/backgrounds/cityBackground.png");

        // Board
        VBox boardBox = Visual.center(boardPane.getGrid());

        // Turn label
        turnLabel = new Label("White's Turn");
        Visual.setLabel(turnLabel, Visual.THALEAH, Color.WHITE, 30);

        // Check label
        checkLabel = new Label("");
        Visual.setLabel(checkLabel, Visual.THALEAH, Color.DARKRED, 30);

        // Labels container
        HBox topLabels = new HBox(15);
        topLabels.setAlignment(Pos.CENTER);
        topLabels.setPadding(new Insets(20, 0, 10, 0));
        topLabels.getChildren().addAll(turnLabel, checkLabel);

        VBox topWrapper = new VBox(10); // spacing between labels and timers
        topWrapper.setAlignment(Pos.CENTER);
        topWrapper.getChildren().add(topLabels);

        // Timers
        if (settings.isTimerEnabled()) {
            int initialTime = settings.getTimerTime();

            whiteTimer = new ChessTimer(initialTime, () -> timeUp(chess.Color.WHITE));
            blackTimer = new ChessTimer(initialTime, () -> timeUp(chess.Color.BLACK));
            
            whiteTimer.resetTimer(initialTime);
            blackTimer.resetTimer(initialTime);

            HBox timerBox = new HBox(550, whiteTimer.getLabel(), blackTimer.getLabel());
            timerBox.setAlignment(Pos.CENTER);

            topWrapper.getChildren().add(timerBox);
        }

        // Create the captured panels for black and white
        whiteCapturedBox = new CapturedPanel(chess.Color.WHITE);
        blackCapturedBox = new CapturedPanel(chess.Color.BLACK);

        // Board with labels and timers
        VBox boardWithLabels = new VBox(10);
        boardWithLabels.setAlignment(Pos.CENTER);
        boardWithLabels.getChildren().addAll(topWrapper, boardBox);

        // Main content layout
        HBox contentBox = new HBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(
            whiteCapturedBox.getContainer(),
            boardWithLabels,
            blackCapturedBox.getContainer()
        );

        HBox.setMargin(whiteCapturedBox.getContainer(), new Insets(60, 0, 0, 0));
        HBox.setMargin(blackCapturedBox.getContainer(), new Insets(60, 0, 0, 0));

        // StackPane for background
        rootPane = new StackPane(bgView, contentBox);
        boardPane.setup();

        // Scene setup
        scene = new Scene(rootPane, (BOARD_SIZE + 6) * TILE_SIZE, (BOARD_SIZE + 3) * TILE_SIZE);

        // Load CSS file for transparent scrollbars
        scene.getStylesheets().add(MainUI.class.getResource("/styles/styles.css").toExternalForm());

        SoundManager.playStart();
        return scene;
    }
    
    // the player whose clock reached 0 loses, unless the other player only has a king (a lone king can't win)
    private static void timeUp(chess.Color player) {
    	if (boardPane.isGameOver()) return;
    	turnLabel.setText("");
    	checkLabel.setText("");
    	SoundManager.playEnd();
    	boardPane.setEndText("Time");
    	EndingUI.show(onlyKingLeft(player.switchColors()) ? null : player, DragAndDrop.getMoveList());
    }
    
    private static boolean onlyKingLeft(chess.Color color) {
    	for (int row = 0; row < 8; row++) {
    		for (int col = 0; col < 8; col++) {
    			chess.Piece piece = boardPane.getBoard().getPiece(row, col);
    			if (piece != null && piece.getColor() == color && !(piece instanceof chess.King)) return false;
    		}
    	}
    	return true;
    }

}