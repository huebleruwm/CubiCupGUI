package CubiCup;

import javafx.scene.control.*;

public class CubiCupEngine extends Engine {

    private ToggleGroup playGroup = new ToggleGroup();
    private ToggleButton playBlue = new ToggleButton("Blue");
    private ToggleButton playGreen = new ToggleButton("Green");
    private ToggleButton playBoth = new ToggleButton("Both");


    private final int EMPTY = -2;
    private final int BASE = -1;
    private final int BLUE = 0;
    private final int GREEN = 1;

    public CubiCupEngine() throws Exception {

        initialize();

        playBlue.setToggleGroup(playGroup);
        playGreen.setToggleGroup(playGroup);
        playBoth.setToggleGroup(playGroup);

        playBlue.focusTraversableProperty().setValue(false);
        playGreen.focusTraversableProperty().setValue(false);
        playBoth.focusTraversableProperty().setValue(false);

        buttonBox.getChildren().addAll(playBlue,playGreen,playBoth);
    }

    public boolean isEngineTurn( int turn ) {

        if( playBlue.isSelected() && turn == BLUE ||
            playGreen.isSelected() && turn == GREEN ||
            playBoth.isSelected() ) {
            return true;
        } else {
            return false;
        }
    }

}
