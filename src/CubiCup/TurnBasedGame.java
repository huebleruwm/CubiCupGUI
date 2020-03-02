package CubiCup;

import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public abstract class TurnBasedGame {

    protected Pane gameDisplay;

    protected ArrayList<Engine> engines = new ArrayList<>();

    protected int turn;

    protected boolean isReady = false;
    protected boolean gameOver = false;

    protected Label hoverLabel = new Label();

    protected Menu[] menuEntries;

    abstract public void startNewGame();

    abstract public void takeTurn( String move );

    abstract public void updateEngines();

    abstract public void initialize();

    abstract public void updateHoverLabel( String labelText );

    abstract public void setupMenuEntries();

    public void sendMoveToEngines( String move ) {
        for( Engine engine : engines ) {
            engine.output(move);
        }
    }

    public void addEngine( Engine eng ) {
        engines.add(eng);
    }

    public int getTurn() {
        return turn;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setGameDisplay( Pane gameDisplay ) {
        this.gameDisplay = gameDisplay;
    }

    public void reset() {
        gameDisplay.getChildren().clear();
        initialize();
    }

    public void setMoveHoverLabel( Label label ) {
        hoverLabel = label;
    }

    public void close() {
        for( Engine engine : engines ) {
            engine.kill();
        }
    }

    public Menu[] getMenuEntries() {
        return menuEntries;
    }
}
