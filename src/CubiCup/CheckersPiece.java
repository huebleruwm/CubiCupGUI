package CubiCup;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class CheckersPiece {

    private final int RED = 0;
    private final int BLACK = 1;

    private int displayX;
    private int displayY;
    private int boardX;
    private int boardY;

    private boolean king = false;

    private int color;

    private Circle pieceCircle;
    private Circle kingCircle;
    private Group pieceView;

    public CheckersPiece( int color, int boardX, int boardY ) {
        this.color = color;
        this.boardX = boardX;
        this.boardY = boardY;
        this.displayY = boardY;

        if( displayY%2 == 0 ) {
            displayX = boardX*2;
        } else {
            displayX = boardX*2 + 1;
        }
        ;
        pieceCircle = new Circle(10);;
        kingCircle = new Circle(10);
        kingCircle.setVisible(false);
        Image img = new Image(getClass().getResourceAsStream("star.png"));
        kingCircle.setFill(new ImagePattern(img));

        pieceView = new Group();
        pieceView.getChildren().addAll(pieceCircle,kingCircle);

        if( color == RED ) {
            pieceCircle.setFill(Color.RED);
        } else {
            pieceCircle.setFill(Color.BLACK);
        }
    }

    public void setPieceRadius( double radius ) {
        pieceCircle.setRadius(radius);
        kingCircle.setRadius(radius*0.9);
    }

    public Node getView() {
        return pieceView;
    }

    public int displayX() {
        return displayX;
    }

    public int displayY() {
        return displayY;
    }

    public int boardX() {
        return boardX;
    }

    public int boardY() {
        return boardY;
    }

    public int color() {
        return color;
    }

    public boolean isKing() {
        return king;
    }

    public void moveTo( int boardX, int boardY ) {

        this.boardX = boardX;
        this.boardY = boardY;
        this.displayY = boardY;

        if( displayY%2 == 0 ) {
            displayX = boardX*2;
        } else {
            displayX = boardX*2 + 1;
        }

        if( !king ) {
            if( color == RED && boardY == 0 ) {
                king();
            } else if( color == BLACK && boardY == 7 ) {
                king();
            }
        }

    }

    private void king() {
        king = true;
        kingCircle.setVisible(true);
    }

    public void select() {
        pieceCircle.setStroke( Color.BLUE );
        pieceCircle.setStrokeWidth( pieceCircle.getRadius() * 0.1 );
    }

    public void unselect() {
        pieceCircle.setStrokeWidth(0);
    }

}
