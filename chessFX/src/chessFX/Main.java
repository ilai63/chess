package chessFX;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
	public static void main(String[] args) {
        launch(args);
    } 
	
	@Override
    public void start(Stage stage) {
		SoundManager.init();
	    // Stage setup
	    stage.setTitle("Chess");
	    stage.getIcons().add(new Image("/pieces/WK.png"));
	    stage.setResizable(false);
	    stage.initStyle(StageStyle.UNDECORATED);
	    stage.setScene(LoadScreenUI.getUI());
	    stage.show();
	    SoundManager.playBGM();
	}
}
