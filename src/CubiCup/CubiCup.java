package CubiCup;

import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.util.Optional;

public class CubiCup extends TurnBasedGame {

    private final int EMPTY = -2;
    private final int BASE = -1;
    private final int BLUE = 0;
    private final int GREEN = 1;

    private int BoxSideLength = 20;

    private int size;
    private int[][][] board;
    private int[] pieces = new int[2];

    private CubiCupDisplay display;

    public CubiCup( Pane gamePane ) {

        setGameDisplay(gamePane);

        startNewGame();

        setupMenuEntries();
    }

    //=============================================
    //                  Setup
    //=============================================

    public void startNewGame() {

        Optional<String> result;

        TextInputDialog sizeInput = new TextInputDialog("7");
        sizeInput.setContentText("Enter board size");
        sizeInput.setHeaderText("");
        sizeInput.setTitle("CubiCup Size Picker");

        while(true) {

            // get size of board
            result = sizeInput.showAndWait();

            // ignore/close if nothing entered
            if( !result.isPresent() ) {
                break;
            }

            try {
                //parse int from user input
                this.size = Integer.parseInt(result.get());

                if( this.size <= 1 ) {
                    sizeInput = new TextInputDialog("7");
                    sizeInput.setHeaderText("");
                    sizeInput.setTitle("CubiCup Size Picker");
                    sizeInput.setContentText("Pick something more than 1 ...");
                } else {
                    gameDisplay.getChildren().clear();
                    initialize();

                    break;
                }
            } catch (Exception e) {
                sizeInput = new TextInputDialog("7");
                sizeInput.setHeaderText("");
                sizeInput.setTitle("CubiCup Size Picker");
                sizeInput.setContentText("You want a board size of '" + result.get() + "'?");
            }
        }
    }

    public void initialize() {

        int totalPieces = size * (size+1) * (size+2) / 6;

        if( totalPieces%2 == 0 ) {
            pieces[BLUE] = totalPieces / 2;
            pieces[GREEN] = totalPieces / 2;
        } else {
            pieces[BLUE] = (totalPieces+1) / 2;
            pieces[GREEN] = (totalPieces-1) / 2;
        }

        board = new int[size+1][size+1][size+1];

        for ( int x = 0; x <= size; x++ ) {
            for ( int y = 0; y <= size; y++ ) {
                for ( int z = 0; z <= size; z++ ) {
                    board[x][y][z] = EMPTY;
                }
            }
        }

        turn = BLUE;

        display = new CubiCupDisplay( size );

        display.addGameToPane( gameDisplay );

        drawBase(size);
        display.addTurnCounters();
        display.updateTurnCounters( pieces[BLUE], pieces[GREEN] );
        display.highlightTurn(turn);
        updateEngines();
        isReady = true;
    }

    public void setupMenuEntries() {
        Menu lightingMenu = new Menu("Lighting");

        Slider pointSlider = new Slider(0,1,0.6);
        Slider ambientSlider = new Slider(0,1,0.3);

        CustomMenuItem pointLight = new CustomMenuItem(pointSlider);
        CustomMenuItem ambientLight = new CustomMenuItem(ambientSlider);

        pointLight.setText("Point");
        ambientLight.setText("Ambient");

        pointSlider.valueProperty().addListener( (observableValue, oldValue, newValue)
                -> display.setPointBrightness((double)newValue) );

        ambientSlider.valueProperty().addListener( (observableValue, oldValue, newValue)
                -> display.setAmbientBrightness((double) newValue) );

        lightingMenu.getItems().addAll(pointLight, ambientLight);

        Menu[] returnMenus = { lightingMenu };
        menuEntries = returnMenus;
    }

    //=============================================
    //             Helper Functions
    //=============================================

    private int[] parseMoveString( String move ) {
        int[] xyz = {0,0,0};

        String[] coords = move.split(",");
        xyz[0] = Integer.parseInt(coords[0].replaceAll("[ (]",""));
        xyz[1] = Integer.parseInt(coords[1].replace(" ",""));
        xyz[2] = Integer.parseInt(coords[2].replaceAll("[ )]",""));

        return xyz;
    }

    private String encodeMove( int x, int y, int z ) {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    private void addCube( int x, int y, int z, Color color ) {

        SideBox box = new SideBox(BoxSideLength*x, BoxSideLength*y, BoxSideLength*z, BoxSideLength, color);
        box.addToGroup(display.getRoot());

        box.boxXU.setOnMousePressed( event -> this.takeTurn(encodeMove(x-1, y, z )));
        box.boxXU.hoverProperty().addListener( event -> updateHoverLabel(encodeMove(x-1,y,z) ));

        box.boxYU.setOnMousePressed( event -> this.takeTurn(encodeMove(x, y-1, z )));
        box.boxYU.hoverProperty().addListener( event -> updateHoverLabel(encodeMove(x,y-1,z) ));

        box.boxZU.setOnMousePressed( event -> this.takeTurn(encodeMove(x, y, z-1 )));
        box.boxZU.hoverProperty().addListener( event -> updateHoverLabel(encodeMove(x,y,z-1) ));
    }

    private void drawBase( int  size ) {

        for( int x = 0; x <= size; x++ ) {
            for( int y = size-x, z = 0; y >= 0; y--,z++ ) {
                //System.out.println(x + " , " + y + " , " + z);
                addCube(x,y,z,Color.TAN);
                board[x][y][z] = BASE;
            }
        }

    }

    public void updateHoverLabel( String move ) {

        int[] xyz = parseMoveString(move);
        int x = xyz[0];
        int y = xyz[1];
        int z = xyz[2];

        if( x < 0 || y < 0 || z < 0 ) {
            hoverLabel.setText("");
        } else {
            hoverLabel.setText("Mouse Over: " + x + "," +  y + "," + z);
        }
    }

    public void updateEngines() {
        //TODO implement update logic, assumes update is new game
        for( Engine engine : engines ) {
            engine.output("newGame:" + size);
        }
    }

    //=============================================
    //                  Rules
    //=============================================

    public void takeTurn( String move ) {

        //System.out.println(x + " , " + y + " , " + z);

        int[] xyz = parseMoveString(move);
        int x = xyz[0];
        int y = xyz[1];
        int z = xyz[2];

        //cant play if game is over
        if( gameOver ) {
            return;
        }

        //cant play off board
        if( x < 0 || y < 0 || z < 0 ) {
            return;
        }

        //cant play if no spot beneath to
        if( board[x+1][y][z] == EMPTY || board[x][y+1][z] == EMPTY || board[x][y][z+1] == EMPTY ) {
            return;
        }

        //cant play if spot is occupied
        if( board[x][y][z] != EMPTY ) {
            return;
        }

        if( turn == BLUE) {
            turn = GREEN;
            pieces[BLUE]--;
            board[x][y][z] = BLUE;
            addCube(x,y,z,Color.BLUE);
            fill( x, y, z,BLUE );
        } else {
            turn = BLUE;
            pieces[GREEN]--;
            board[x][y][z] = GREEN;
            addCube(x,y,z,Color.GREEN);
            fill( x, y, z,GREEN );
        }

        display.updateTurnCounters( pieces[BLUE], pieces[GREEN] );
        display.highlightTurn(turn);
        checkForEnd();
        sendMoveToEngines("move:" + x + "," + y + "," + z);
    }

    private void checkForEnd() {

        if( board[0][0][0] != EMPTY ) {
            //someone reached top
            if( board[1][0][0] == board[0][1][0] && board[1][0][0] == board[0][0][1] && board[0][0][0] != board[1][0][0] ) {
                //this is a tie
                display.setTie();
                gameOver = true;
            } else {
                //this is a win
                if( board[0][0][0] == BLUE ) {
                    display.setWinner(BLUE);
                    gameOver = true;
                } else {
                    display.setWinner(GREEN);
                    gameOver = true;
                }
            }
        } else {

            //check for win by empty pieces
            if (pieces[BLUE] == 0 && turn == BLUE) {
                //player 1 ran out of pieces, player 2 wins
                display.setWinner(GREEN);
                gameOver = true;
            } else if (pieces[GREEN] == 0 && turn == GREEN) {
                //player 2 ran out of pieces, player 1 wins
                display.setWinner(BLUE);
                gameOver = true;
            }
        }
    }

    private void fill( int x, int y, int z, int lastTurnAdded ) {

        int thisTurnAdded;
        Color fillColor;

        if ( lastTurnAdded == BLUE ) {
            thisTurnAdded = GREEN;
            fillColor = Color.GREEN;
        } else {
            thisTurnAdded = BLUE;
            fillColor = Color.BLUE;
        }

        if( pieces[thisTurnAdded] != 0 ) {
            if (x > 0 && board[x - 1][y + 1][z] == board[x][y][z] && board[x - 1][y][z + 1] == board[x][y][z]) {
                addCube(x - 1, y, z, fillColor);
                board[x - 1][y][z] = thisTurnAdded;
                pieces[thisTurnAdded]--;

                if (pieces[lastTurnAdded] != 0) {
                    //other player can still fill
                    fill(x - 1, y, z, thisTurnAdded);
                }
            }
        }

        if( pieces[thisTurnAdded] != 0 ) {
            if (y > 0 && board[x + 1][y - 1][z] == board[x][y][z] && board[x][y - 1][z + 1] == board[x][y][z]) {
                addCube(x, y - 1, z, fillColor);
                board[x][y - 1][z] = thisTurnAdded;
                pieces[thisTurnAdded]--;

                if (pieces[lastTurnAdded] != 0) {
                    //other player can still fill
                    fill(x, y - 1, z, thisTurnAdded);
                }
            }
        }

        if( pieces[thisTurnAdded] != 0 ) {
            if (z > 0 && board[x + 1][y][z - 1] == board[x][y][z] && board[x][y + 1][z - 1] == board[x][y][z]) {
                addCube(x, y, z - 1, fillColor);
                board[x][y][z - 1] = thisTurnAdded;
                pieces[thisTurnAdded]--;

                if (pieces[lastTurnAdded] != 0) {
                    //other player can still fill
                    fill(x, y, z - 1, thisTurnAdded);
                }
            }
        }
    }
}
