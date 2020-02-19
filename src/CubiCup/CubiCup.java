package CubiCup;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.BufferedWriter;
import java.util.ArrayList;

public class CubiCup {

    private final int EMPTY = -2;
    private final int BASE = -1;
    private final int BLUE = 0;
    private final int GREEN = 1;

    int size;
    int turn = BLUE;
    int[][][] board;
    int[] pieces = new int[2];

    Pane gameDisplay;
    private GameDisplay display;

    Text blueCounter;
    Text greenCounter;
    int counterFont = 35;

    Rectangle rectB;
    Rectangle rectG;

    int BoxSideLength = 20;

    Boolean gameOver = false;

    ArrayList<BufferedWriter> engineOutputs;

    Label spotHover = new Label();

    public CubiCup(Pane gamePane, int size) {

       this.size = size;
       this.gameDisplay = gamePane;

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

        display = new GameDisplay( size );

        display.addGameToPane( gamePane );

        drawBase(size);
        addTurnCounters();

        //System.out.println( "P1  P2" );
        //System.out.println( pieces[BLUE] + "  " + pieces[GREEN] );

    }

    private void addTurnCounters() {

        rectB = new Rectangle();
        rectB.setWidth(150);
        rectB.setHeight(150);
        rectB.setFill(Color.BLUE);
        rectB.setStroke(Color.DARKBLUE);

        rectG = new Rectangle();
        rectG.setWidth(150);
        rectG.setHeight(150);
        rectG.setFill(Color.GREEN);
        rectG.setStroke(Color.DARKGREEN);

        StackPane blue = new StackPane();
        blueCounter = new Text(""+pieces[BLUE]);
        blueCounter.setFont(new Font(counterFont));
        blue.getChildren().addAll( rectB, blueCounter );

        StackPane green = new StackPane();
        greenCounter = new Text(""+pieces[GREEN]);
        greenCounter.setFont(new Font(counterFont));
        green.getChildren().addAll( rectG, greenCounter );

        gameDisplay.getChildren().addAll( blue, green );

        blue.setTranslateY(gameDisplay.getHeight() * 0.1);
        green.setTranslateY(gameDisplay.getHeight() * 0.1);
        blue.setTranslateX(gameDisplay.getWidth() * 0.2-75);
        green.setTranslateX(gameDisplay.getWidth() * 0.8-75);

        gameDisplay.heightProperty().addListener((obs, oldVal, newVal) -> {
            blue.setTranslateY((double)newVal * 0.1);
            green.setTranslateY((double)newVal * 0.1);
        });

        gameDisplay.widthProperty().addListener((obs, oldVal, newVal) -> {
            blue.setTranslateX((double)newVal * 0.2-75);
            green.setTranslateX((double)newVal * 0.8-75);
        });
    }

    private void updateCounters() {
        blueCounter.setText(""+pieces[BLUE]);
        greenCounter.setText(""+pieces[GREEN]);
    }

    public void setSpotHoverLabel( Label label ) {
        spotHover = label;
    }

    private void takeTurn( int x, int y, int z ) {

        //System.out.println(x + " , " + y + " , " + z);

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

        updateCounters();
        checkForEnd();

        for( BufferedWriter engineOutput : engineOutputs ) {
            try {
                engineOutput.write("move:" + x + "," + y + "," + z);
                engineOutput.newLine();
                engineOutput.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //System.out.println( "P1  P2" );
        //System.out.println( pieces[BLUE] + "  " + pieces[GREEN] );
    }

    public void setEngineOutputs( ArrayList<BufferedWriter> outputs ) {
        engineOutputs = outputs;
    }

    private void checkForEnd() {

        if( board[0][0][0] != EMPTY ) {
            //someone reached top
            if( board[1][0][0] == board[0][1][0] && board[1][0][0] == board[0][0][1] && board[0][0][0] != board[1][0][0] ) {
                //this is a tie
                setTie();
            } else {
                //this is a win
                if( board[0][0][0] == BLUE ) {
                    setWinner(BLUE);
                } else {
                    setWinner(GREEN);
                }
            }
        } else {

            //check for win by empty pieces
            if (pieces[BLUE] == 0) {
                //player 1 ran out of pieces, player 2 wins
                setWinner(GREEN);
            } else if (pieces[GREEN] == 0) {
                //player 2 ran out of pieces, player 1 wins
                setWinner(BLUE);
            }

        }

    }

    private void setWinner( int winner ) {

        if( winner == BLUE ) {
            rectB.setStroke(Color.GOLD);
            rectB.setStrokeWidth(5);
        } else {
            rectG.setStroke(Color.GOLD);
            rectG.setStrokeWidth(5);
        }

        gameOver = true;
    }

    private void setTie() {

        rectB.setStroke(Color.SILVER);
        rectB.setStrokeWidth(5);

        rectG.setStroke(Color.SILVER);
        rectG.setStrokeWidth(5);

        gameOver = true;
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

    private void addCube( int x, int y, int z, Color color ) {

        SideBox box = new SideBox(BoxSideLength*x, BoxSideLength*y, BoxSideLength*z, BoxSideLength, color);
        box.addToGroup(display.getRoot());

        box.boxXU.setOnMousePressed( event -> this.takeTurn(x-1, y, z ));
        box.boxXU.hoverProperty().addListener( event -> updateLabel(x-1,y,z) );

        box.boxYU.setOnMousePressed( event -> this.takeTurn(x, y-1, z ));
        box.boxYU.hoverProperty().addListener( event -> updateLabel(x,y-1,z) );

        box.boxZU.setOnMousePressed( event -> this.takeTurn(x, y, z-1 ));
        box.boxZU.hoverProperty().addListener( event -> updateLabel(x,y,z-1) );
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

    private void updateLabel( int x, int y, int z ) {

        if( x < 0 || y < 0 || z < 0 ) {
            spotHover.setText("");
        } else {
            spotHover.setText("Mouse Over: " + x + "," +  y + "," + z);
        }
    }

}
