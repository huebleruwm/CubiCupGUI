package CubiCup;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;

public class Checkers extends TurnBasedGame {

    private final int EMPTY = -2;
    private final int RED = 0;
    private final int BLACK = 1;

    private int[][] board;
    private CheckersDisplay display;
    private Pane boardSpots[][];
    private CheckersPiece[][] pieces;

    private CheckersPiece selectedPiece;
    private boolean selectedPieceCanJump;
    private boolean turnChangeOnUnselect = false;

    private ArrayList<String> movesForSelectedPiece;

    public Checkers( Pane gamePane ) {

        setGameDisplay(gamePane);

        startNewGame();

        setupMenuEntries();
    }

    public void startNewGame() {
        gameDisplay.getChildren().clear();
        initialize();
    }

    public Engine createEngine() {
        return null;
    }

    public void takeTurn(String move) {

        int[] xyToxy = parseMove(move);

        int xFrom = xyToxy[0];
        int yFrom = xyToxy[1];
        int xTo = xyToxy[2];
        int yTo = xyToxy[3];

        board[xTo][yTo] = board[xFrom][yFrom];
        board[xFrom][yFrom] = EMPTY;

        pieces[xTo][yTo] = pieces[xFrom][yFrom];
        pieces[xFrom][yFrom] = null;

        display.removePiece(pieces[xTo][yTo]);
        pieces[xTo][yTo].moveTo( xTo, yTo );
        display.addPiece(pieces[xTo][yTo]);

        if( Math.abs(yTo-yFrom) == 2 ) {

            // A piece is being captured

            // Remove jumped piece
            int yJumped = (yTo+yFrom)/2;
            int xJumped = xTo;

            if( xTo > xFrom ) {
                if( yFrom%2 == 0 ) {
                    xJumped = xFrom;
                }
            } else {
                if( yFrom%2 == 1 ) {
                    xJumped = xFrom;
                }
            }

            board[xJumped][yJumped] = EMPTY;
            display.removePiece(pieces[xJumped][yJumped]);
            pieces[xJumped][yJumped] = null;

            // Update moves
            updateMovesForSelectedPiece();
        }

        // If selected piece can't jump, or didn't jump, turn must be over
        if( !selectedPieceCanJump || Math.abs(yTo-yFrom) == 1 ) {
            changeTurn();
            selectedPiece.unselect();
            selectedPiece = null;
            turnChangeOnUnselect = false;
        } else {
            // Move must have been a jump, and other jump possible
            turnChangeOnUnselect = true;
        }

        checkForEnd();

        System.out.println( "Move:"+move);

    }

    private void checkForEnd() {

        boolean anyMovesFound = false;
        boolean anyBlackPieces = false;
        boolean anyRedPieces = false;

        for ( int x = 0; x < 4; x++ ) {
            for ( int y = 0; y < 8; y++ ) {
                if( pieces[x][y] != null ) {

                    if( !anyMovesFound && pieces[x][y].color() == turn && getAvailableMovesForPiece(pieces[x][y]).size() > 0 ) {

                        anyMovesFound = true;
                    }

                    if( pieces[x][y].color() == RED ) {
                        anyRedPieces = true;
                    }

                    if( pieces[x][y].color() == BLACK ) {
                        anyBlackPieces = true;
                    }

                    if( anyRedPieces && anyBlackPieces && anyMovesFound ) {
                        // Both players have at least one pieces, and a move has been found, game not over
                        return;
                    }
                }
            }
        }

        // if we get here then game must be over
        gameOver = true;

        if( anyRedPieces && anyBlackPieces ) {
            // Both players have pieces, but no moves available, must be a tie
            display.indicateTie();
        } else if( anyBlackPieces ) {
            // Black wins
            display.indicateWinner(BLACK);
        } else {
            // Red wins
            display.indicateWinner(RED);
        }

    }

    private void changeTurn() {
        if( turn == RED ) {
            turn = BLACK;
        } else {
            turn = RED;
        }
    }

    public int[] parseMove( String move ) {
        String[] moveToFrom = move.split(">");
        int[] xyToxy = new int[4];

        String[] fromXY = moveToFrom[0].split(",");
        String[] toXY = moveToFrom[1].split(",");

        xyToxy[0] = Integer.parseInt(fromXY[0].replaceAll("[ (]",""));
        xyToxy[1] = Integer.parseInt(fromXY[1].replaceAll("[ )]",""));
        xyToxy[2] = Integer.parseInt(toXY[0].replaceAll("[ (]",""));
        xyToxy[3] = Integer.parseInt(toXY[1].replaceAll("[ )]",""));

        return xyToxy;
    }

    public void updateEngines() {

    }

    public void initialize() {

        board = new int[4][8];
        pieces = new CheckersPiece[4][8];

        for ( int x = 0; x < 4; x++ ) {
            for ( int y = 0; y < 8; y++ ) {
                board[x][y] = EMPTY;
            }
        }

        turn = RED;

        display = new CheckersDisplay();
        display.addGameToPane( gameDisplay );

        movesForSelectedPiece = new ArrayList<>();

        setupBoardSpots();
        addInitialPieces();
        updateEngines();

        if( hoverLabel != null ) {
            setMoveHoverLabel(hoverLabel);
        }

        isReady = true;
        gameOver = false;
    }

    public void setupBoardSpots() {

        boardSpots = display.getBoardSpots();

        for ( int x = 0; x < 4; x++ ) {
            for ( int y = 0; y < 8; y++ ) {
                int yBoard = y;
                int xBoard = x;
                boardSpots[x][y].setOnMouseClicked( event -> {
                    moveSelectedPieceTo(xBoard,yBoard);
                });
            }
        }
    }


    public void updateHoverLabel(String labelText) {

    }

    public void setupMenuEntries() {

    }

    public void setMoveHoverLabel( Label label ) {
        super.setMoveHoverLabel(label);
        display.setHoverPositionsForLabel(label);
    }

    public void addInitialPieces() {

        for ( int x = 0; x < 4; x++ ) {
            for ( int y = 0; y < 3; y++ ) {
                CheckersPiece piece = new CheckersPiece(BLACK, x, y);
                pieces[x][y] = piece;
                display.addPiece( piece );
                piece.getView().setOnMouseClicked( event -> {
                    selectPiece(piece);
                });
                board[x][y] = BLACK;
            }
        }

        for ( int x = 0; x < 4; x++ ) {
            for ( int y = 5; y < 8; y++ ) {
                CheckersPiece piece = new CheckersPiece(RED, x, y);
                pieces[x][y] = piece;
                display.addPiece( piece  );
                piece.getView().setOnMouseClicked( event -> {
                    selectPiece(piece);
                });
                board[x][y] = RED;
            }
        }
    }

    private void moveSelectedPieceTo( int x, int y ) {

        if( selectedPiece != null ) {

            int pieceX = selectedPiece.boardX();
            int pieceY = selectedPiece.boardY();

            String attemptedMove = "(" + pieceX + ", " + pieceY + ")>(" + x + ", " + y + ")";

            if( movesForSelectedPiece.contains(attemptedMove) ) {
                takeTurn(attemptedMove);
            }

        }

    }

    private void selectPiece( CheckersPiece piece ) {

        if( gameOver ) {
            // Don't select if gameover
            return;
        }

        if( turnChangeOnUnselect ) {
            turnChangeOnUnselect = false;
            selectedPiece.unselect();
            selectedPiece = null;
            changeTurn();
            System.out.println("Move:pass");
            return;
        }

        if( piece.color() != turn ) {
            return;
        }

        if( selectedPiece != null ) {
            selectedPiece.unselect();
        }

        if( selectedPiece != null && selectedPiece == piece) {
            selectedPiece = null;
            return;
        } else {
            selectedPiece = piece;
            selectedPiece.select();
        }

        //Line line = new Line();
        //display.setLinePosition( line, selectedPiece.displayX(), selectedPiece.displayY(), selectedPiece.displayX()-1, selectedPiece.displayY() );

        updateMovesForSelectedPiece();

    }

    private void updateMovesForSelectedPiece() {
        movesForSelectedPiece = getAvailableMovesForPiece(selectedPiece);
    }

    private ArrayList getAvailableMovesForPiece( CheckersPiece piece ) {

        ArrayList<String> moves = new ArrayList<>();

        int x = piece.boardX();
        int y = piece.boardY();
        int opposite;

        selectedPieceCanJump = false;

        if( piece.color() == RED ) {
            opposite = BLACK;
        } else {
            opposite = RED;
        }

        if( (piece.color() == RED || piece.isKing()) && y-1 >= 0 )  {

            if( y%2 == 0 ) {

                if( x-1 >= 0 ) {
                    if( board[x-1][y-1] == EMPTY ){
                        moves.add( encodeMove(x,y,x-1,y-1) );
                    } else if( y-2 >= 0 && board[x-1][y-1] == opposite && board[x-1][y-2] == EMPTY ) {
                        moves.add( encodeMove(x,y,x-1,y-2) );
                        selectedPieceCanJump = true;
                    }
                }

                if( board[x][y-1] == EMPTY ){
                    moves.add( encodeMove(x,y,x,y-1) );
                } else if( x+1 <= 3 && y-2 >= 0 && board[x][y-1] == opposite && board[x+1][y-2] == EMPTY ) {
                    moves.add( encodeMove(x,y,x+1,y-2) );
                    selectedPieceCanJump = true;
                }

            } else {

                if( x+1 <= 3 ) {
                    if( board[x+1][y-1] == EMPTY ){
                        moves.add( encodeMove(x,y,x+1,y-1) );
                    } else if( y-2 >= 0 && board[x+1][y-1] == opposite && board[x+1][y-2] == EMPTY ) {
                        moves.add( encodeMove(x,y,x+1,y-2) );
                        selectedPieceCanJump = true;
                    }
                }

                if( board[x][y-1] == EMPTY ){
                    moves.add( encodeMove(x,y,x,y-1) );
                } else if( x-1 >= 0 && y-2 >= 0 && board[x][y-1] == opposite && board[x-1][y-2] == EMPTY ) {
                    moves.add( encodeMove(x,y,x-1,y-2) );
                    selectedPieceCanJump = true;
                }
            }
        }

        if( (piece.color() == BLACK || piece.isKing()) && y+1 <= 7 ) {
            //Piece is black
            if( y%2 == 0 ) {

                if( x-1 >= 0 ) {
                    if( board[x-1][y+1] == EMPTY ){
                        moves.add( encodeMove(x,y,x-1,y+1) );
                    } else if( y+2 <= 7 && board[x-1][y+1] == opposite && board[x-1][y+2] == EMPTY ) {
                        moves.add( encodeMove(x,y,x-1,y+2) );
                        selectedPieceCanJump = true;
                    }
                }

                if( board[x][y+1] == EMPTY ){
                    moves.add( encodeMove(x,y,x,y+1) );
                } else if( x+1 <= 3 && y+2 <= 7 && board[x][y+1] == opposite && board[x+1][y+2] == EMPTY ) {
                    moves.add( encodeMove(x,y,x+1,y+2) );
                    selectedPieceCanJump = true;
                }

            } else {

                if( x+1 <= 3 ) {
                    if( board[x+1][y+1] == EMPTY ){
                        moves.add( encodeMove(x,y,x+1,y+1) );
                    } else if( y+2 <= 7 && board[x+1][y+1] == opposite && board[x+1][y+2] == EMPTY ) {
                        moves.add( encodeMove(x,y,x+1,y+2) );
                        selectedPieceCanJump = true;
                    }
                }

                if( board[x][y+1] == EMPTY ){
                    moves.add( encodeMove(x,y,x,y+1) );
                } else if( x-1 >= 0 && y+2 >= 0 && board[x][y+1] == opposite && board[x-1][y+2] == EMPTY ) {
                    moves.add( encodeMove(x,y,x-1,y+2) );
                    selectedPieceCanJump = true;
                }
            }
        }

        // Debugging to print potential moves
        //for( String move : movesForSelectedPiece ) {
        //    System.out.println(move);
        //}

        return moves;
    }

    private String encodeMove( int xFrom, int yFrom, int xTo, int yTo ) {
        return "(" + xFrom + ", " + yFrom + ")>(" + xTo + ", " + yTo + ")";
    }

}
