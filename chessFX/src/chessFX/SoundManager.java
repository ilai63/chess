package chessFX;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class SoundManager {

    private static MediaPlayer movePlayer;
    private static MediaPlayer capturePlayer;
    private static MediaPlayer checkPlayer;
    private static MediaPlayer endPlayer;
    private static MediaPlayer startPlayer;
    private static MediaPlayer illegalPlayer;
    private static MediaPlayer castlePlayer;
    private static MediaPlayer promotionPlayer;
    private static MediaPlayer bgmPlayer;
    
    public static void init() {
        // loaded from the classpath ("/sounds/..."), a file path like "src/sounds" only works when running from Eclipse
        movePlayer = createPlayer("/sounds/move.mp3");
        capturePlayer = createPlayer("/sounds/capture.mp3");
        checkPlayer = createPlayer("/sounds/check.mp3");
        endPlayer = createPlayer("/sounds/game-end.mp3");
        startPlayer = createPlayer("/sounds/game-start.mp3");
        illegalPlayer = createPlayer("/sounds/illegal.mp3");
        castlePlayer = createPlayer("/sounds/castle.mp3");
        promotionPlayer = createPlayer("/sounds/promote.mp3");

        // Background music setup
        bgmPlayer = createPlayer("/sounds/background.mp3");
        if (bgmPlayer != null) {
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop indefinitely
            bgmPlayer.setVolume(0.1);
        }
    }
    
    // one sound that fails to load shouldn't turn off all the others
    private static MediaPlayer createPlayer(String path) {
        try {
            URL url = SoundManager.class.getResource(path);
            if (url == null) {
                System.err.println("Sound not found: " + path);
                return null;
            }
            return new MediaPlayer(new Media(url.toExternalForm()));
        } catch (Exception e) {
            System.err.println("Couldn't load sound " + path + ": " + e.getMessage());
            return null;
        }
    }
    
    private static void stop(MediaPlayer player) {
        if (player != null) player.stop();
    }
    
    public static void playBGM() {
        if (bgmPlayer != null) bgmPlayer.play();
    }

    public static void stopBGM() {
        if (bgmPlayer != null) bgmPlayer.stop();
    }

    public static void playMove() {
        if (movePlayer != null) {
            movePlayer.stop();
            movePlayer.play();
        }
    }
    
    public static void playCapture() {
        if (capturePlayer != null) {
        	capturePlayer.stop();
        	stop(movePlayer);
        	capturePlayer.play();
        }
    }
    
    public static void playCheck() {
        if (checkPlayer != null) {
            stop(movePlayer);
            stop(capturePlayer);
            checkPlayer.stop();
            checkPlayer.play();
        }
    }
    
    public static void playEnd() {
        if (endPlayer != null) {
        	stop(movePlayer);
            stop(capturePlayer);
            stop(checkPlayer);
            endPlayer.stop();
            endPlayer.play();
        }
    }
    
    public static void playStart() {
        if (startPlayer != null) {
        	startPlayer.stop(); // a player that reached the end doesn't play again without stop()
        	startPlayer.play();
        }
    }
    
    public static void playIllegal() {
        if (illegalPlayer != null) {
        	illegalPlayer.stop();
        	illegalPlayer.play();
        }
    }
    
    public static void playCastle() {
        if (castlePlayer != null) {
            stop(movePlayer);
            castlePlayer.stop();
            castlePlayer.play();
        }
    }
    
    public static void playPromotion() {
        if (promotionPlayer != null) {
            stop(movePlayer);
            promotionPlayer.stop();
            promotionPlayer.play();
        }
    }
}