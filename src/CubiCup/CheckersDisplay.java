package CubiCup;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class CheckersDisplay {

    private Pane gameDisplay;

    private GridPane checkersBoard;
    private Pane[][] gridPanes;
    private ArrayList<CheckersPiece> pieces;

    private double paneSize;

    private final int RED = 0;
    private final int BLACK = 1;

    public CheckersDisplay() {

        buildBoard();

    }

    private void buildBoard() {

        checkersBoard = new GridPane();
        gridPanes = new Pane[8][8];
        pieces = new ArrayList<>();

        for( int x = 0; x < 8; x++ ) {
            for( int y = 0; y < 8; y++ ) {
                gridPanes[x][y] = new Pane();
                gridPanes[x][y].setBorder(new Border(new BorderStroke(Color.BLACK,
                        BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                checkersBoard.add( gridPanes[x][y], x, y );

                if( (x+y)%2 == 0 ) {
                    gridPanes[x][y].setBackground(new Background(new BackgroundFill(Color.GREY,CornerRadii.EMPTY, Insets.EMPTY)));
                }

            }
            checkersBoard.getColumnConstraints().add(new ColumnConstraints(100));
            checkersBoard.getRowConstraints().add(new RowConstraints(100));
        }

    }

    public void addGameToPane( Pane pane ) {

        gameDisplay = pane;

        gameDisplay.getChildren().add(checkersBoard);

        updateGridPaneSize();

        gameDisplay.heightProperty().addListener((obs, oldVal, newVal) -> {
            updateGridPaneSize();
        });

        gameDisplay.widthProperty().addListener((obs, oldVal, newVal) -> {
            updateGridPaneSize();
        });

    }

    private void updateGridPaneSize() {

        int padding = 10;

        paneSize = Math.min( gameDisplay.getHeight(), gameDisplay.getWidth());

        paneSize = (paneSize - 2*padding) / 8;

        checkersBoard.getRowConstraints().clear();
        checkersBoard.getColumnConstraints().clear();
        for( int x = 0; x < 8; x ++ ) {
            checkersBoard.getRowConstraints().add( new RowConstraints(paneSize));
            checkersBoard.getColumnConstraints().add( new ColumnConstraints(paneSize));
        }

        checkersBoard.setPadding(new Insets(padding,padding,padding,padding));

        centerPieces();
    }

    public void addPiece(CheckersPiece piece) {
        gridPanes[piece.displayX()][piece.displayY()].getChildren().add(piece.getView());
        pieces.add(piece);
        piece.getView().setTranslateX(paneSize / 2);
        piece.getView().setTranslateY(paneSize / 2);
        piece.setPieceRadius(paneSize/2.5);
    }

    public void removePiece( CheckersPiece piece ) {
        gridPanes[piece.displayX()][piece.displayY()].getChildren().clear();
        pieces.remove(pieces);
    }

    public void centerPieces() {

        for( CheckersPiece piece : pieces ) {
            piece.getView().setTranslateX(paneSize / 2);
            piece.getView().setTranslateY(paneSize / 2);
            piece.setPieceRadius(paneSize/2.5);
        }
    }

    public void setHoverPositionsForLabel( Label label ) {
        for( int x = 0; x < 8; x++ ) {
            for (int y = 0; y < 8; y++) {

                int yBoard = y;
                int xBoard = x;
                Pane pane = gridPanes[x][y];

                if( (x+y)%2 == 0 ) {
                    pane.hoverProperty().addListener(event -> {
                        if (pane.isHover()) {
                            label.setText("Mouse Over: (" + xBoard /2 + ", " + yBoard + ")");
                        } else {
                            label.setText("");
                        }
                    });
                }
            }
        }
    }

    public Pane[][] getBoardSpots() {

        Pane[][] spots = new Pane[4][8];

        for( int x = 0; x < 8; x++ ) {
            for (int y = 0; y < 8; y++) {
                if( (x+y)%2 == 0 ) {
                    spots[x/2][y] = gridPanes[x][y];
                }
            }
        }

        return spots;
    }

    public void indicateWinner( int winner ) {

        StackPane popup = new StackPane();
        Rectangle window = new Rectangle(gameDisplay.getWidth()/2, gameDisplay.getHeight()/4);
        window.setFill(Color.rgb(255,255,255,0.85));
        window.setStrokeWidth(5);
        window.setStroke(Color.BLACK);

        Text winnerText;

        if( winner == RED ) {
            winnerText = new Text("Red Wins!");
        } else {
            winnerText = new Text("Black Wins!");
        }

        winnerText.setFont( new Font(50) );

        popup.getChildren().addAll(window,winnerText);

        gameDisplay.getChildren().add(popup);
        popup.setTranslateX(gameDisplay.getWidth()/4);
        popup.setTranslateY(gameDisplay.getWidth()*3/8);

        gameDisplay.widthProperty().addListener( event -> {
            window.setHeight(gameDisplay.getHeight()/4);
            popup.setTranslateX(gameDisplay.getWidth()/4);
        });

        gameDisplay.heightProperty().addListener( event -> {
            window.setWidth(gameDisplay.getWidth()/2);
            popup.setTranslateY(gameDisplay.getWidth()*3/8);
        });

    }

    public void indicateTie() {
        StackPane popup = new StackPane();
        Rectangle window = new Rectangle(gameDisplay.getWidth()/2, gameDisplay.getHeight()/4);
        window.setFill(Color.rgb(255,255,255,0.85));
        window.setStrokeWidth(5);
        window.setStroke(Color.BLACK);

        Text winnerText = new Text("Tie");

        winnerText.setFont( new Font(50) );

        popup.getChildren().addAll(window,winnerText);

        gameDisplay.getChildren().add(popup);
        popup.setTranslateX(gameDisplay.getWidth()/4);
        popup.setTranslateY(gameDisplay.getHeight()*3/8);

        gameDisplay.widthProperty().addListener( event -> {
            window.setWidth(gameDisplay.getWidth()/2);
            popup.setTranslateX(gameDisplay.getWidth()/4);
        });

        gameDisplay.heightProperty().addListener( event -> {
            window.setHeight(gameDisplay.getHeight()/4);
            popup.setTranslateY(gameDisplay.getHeight()*3/8);
        });
    }


}
