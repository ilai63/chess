package chessFX;
import chess.*;

public class GameSettings {
	private boolean isBotSelected;
	private boolean isTimerSelected;
	private Color botTurn;
	private int timerTime;
	
	public GameSettings(boolean isBotSelected, boolean isTimerSelected, Color botTurn, int timerTime) {
		this.isBotSelected = isBotSelected;
		this.isTimerSelected = isTimerSelected;
		this.botTurn = botTurn;
		this.timerTime = timerTime;
	}
	
	public GameSettings(GameSettings other) {
		this(other.isBotSelected, other.isTimerSelected, other.botTurn, other.timerTime);
	}
	
	public boolean isTimerEnabled() {
		return isTimerSelected;
	}
	
	public boolean isBotEnabled() {
		return isBotSelected;
	}
	
	public int getTimerTime() {
		return timerTime;
	}
	
	public Color getBotTurn() {
		return botTurn;
	}
	
	public void setTimerSetting(boolean setting) {
		isTimerSelected = setting;
	}
	
	public void setBotSetting(boolean setting) {
		isBotSelected = setting;
	}
	
	public void setTimerTime(int time) {
		timerTime = time;
	}
	
	public void setBotTurn(Color turn) {
		botTurn = turn;
	}
}
