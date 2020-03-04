package CubiCup;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class GameController {

    @FXML private Pane gamePane;
    @FXML private VBox sidePane;

    private TurnBasedGame game;

    private Class[] availableGames = { CubiCup.class,
                                       Checkers.class };

    private Tab parentTab;

    private Label leftLabel;

    private MenuBar menu;

    public void initialize() {

        addAvailableGames();
    }

    public void setTab( Tab parentTab ) {
        this.parentTab = parentTab;
    }

    public void addAvailableGames() {

        gamePane.getChildren().clear();

        VBox gameChooser = new VBox();
        gameChooser.setAlignment( Pos.CENTER );
        gameChooser.prefWidthProperty().bind( gamePane.widthProperty() );
        gameChooser.prefHeightProperty().bind( gamePane.prefHeightProperty() );
        gameChooser.getChildren().add( new Text("Select Game") );

        for( Class gameClass : availableGames ) {

            String gameName = gameClass.getName().substring(gameClass.getName().lastIndexOf(".") + 1);

            Button gameButton = new Button(gameName);
            gameButton.setPrefHeight(50);
            gameButton.setPrefWidth(100);

            gameButton.setOnAction( event -> {
                gamePane.getChildren().clear();
                try {
                    game = (TurnBasedGame)gameClass.getConstructor(Pane.class).newInstance(gamePane);
                    if( game.isReady() ) {
                        game.setMoveHoverLabel(leftLabel);
                        parentTab.setText(gameName);
                        initSidePanel();
                        if( game.getMenuEntries() != null ) {
                            menu.getMenus().addAll(game.getMenuEntries());
                            parentTab.setOnSelectionChanged( tabEvent -> {
                                if( parentTab.isSelected() ) {
                                    menu.getMenus().addAll(game.getMenuEntries());
                                } else {
                                    // Run later required for some weird jdk bug
                                    Platform.runLater(() -> {
                                        menu.getMenus().removeAll(game.getMenuEntries());
                                    });
                                }
                            });
                        }
                    } else {
                        addAvailableGames();
                    }
                } catch( Exception e ) {
                    System.out.println("Error in addAvailableGames");
                    e.printStackTrace();
                }
            });

            gameChooser.getChildren().add( gameButton );
        }

        gamePane.getChildren().add( gameChooser );

    }

    public void setLeftLabel( Label label ) {
        leftLabel = label;
    }

    public void close() {
        if( game != null ) {
            game.close();
        }
    }

    public void setMenu( MenuBar menu ) {
        this.menu = menu;
    }

    public void initSidePanel() {

        Button newGame = new Button("New Game");
        Button reset = new Button("Reset Game");
        HBox gameControls = new HBox();

        newGame.setPrefWidth(sidePane.getWidth());
        reset.setPrefWidth(sidePane.getWidth());
        gameControls.setPrefWidth(sidePane.getWidth());

        newGame.setOnMouseClicked( event -> {
            game.startNewGame();
            enginePlay.restart();
        });
        reset.setOnMouseClicked( event -> {
            game.reset();
            enginePlay.restart();
        } );
        gameControls.getChildren().addAll( newGame, reset );

        sidePane.getChildren().add( 0, gameControls );

        Button addEngine = new Button("+");
        addEngine.setMaxWidth(3e8);

        addEngine.setOnAction( event -> {

            try {
                Engine eng = game.createEngine();
                if( eng != null ) {
                    int pos = sidePane.getChildren().size() - 1;

                    sidePane.getChildren().add(pos, eng.getDropDown());
                    eng.getDropDown().setExpanded(true);

                    eng.closeButton().setOnAction(click -> {
                        sidePane.getChildren().remove(pos);
                        eng.kill();
                        game.removeEngine(eng);
                        enginePlay.restart();
                    });

                    eng.resetButton().setOnAction(click -> {
                        eng.reset();
                        game.updateEngines();
                        enginePlay.restart();
                    });

                    game.addEngine(eng);
                    game.updateEngines();

                    enginePlay.restart();
                }

            } catch( Exception e ) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        });

        sidePane.getChildren().add( addEngine );
    }

    Service<Void> enginePlay = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {

            return new Task<Void>() {

                @Override
                protected Void call() {

                    while( true ) {

                        try {


                            while( !game.isReady() || game.isGameOver() ) {
                                Thread.sleep(100);
                            }

                            for( Engine engine : game.engines() ) {

                                // If it is engines turn
                                if( engine.isEngineTurn(game.getTurn()) ) {
                                    // Wait think time
                                    Thread.sleep(engine.getThinkTime_ms());

                                    // If it is still engine's turn after thinking, play the best move
                                    if( engine.isEngineTurn(game.getTurn()) ) {

                                        int turnSave = game.getTurn();

                                        //play move
                                        Platform.runLater(() -> {
                                            game.takeTurn(engine.getBestMove());
                                        });

                                        while( turnSave == game.getTurn() ) {
                                            // Wait until turn for game changes
                                            Thread.sleep(100);
                                        }

                                    }
                                }
                            }

                            // Sleep a little bit after servicing engines
                            Thread.sleep(100);

                        } catch( Exception e ) {
                            e.printStackTrace();
                            System.out.println( "enginePlay interrupted" );
                        }
                    }
                }
            };
        }

        @Override
        protected void succeeded() {
            reset();
        }
    };
}
