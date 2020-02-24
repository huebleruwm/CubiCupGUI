package CubiCup;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.TimerTask;

public class Controller {

    @FXML
    private Pane gamePane;
    private CubiCup game;

    @FXML
    private VBox sidePane;

    @FXML
    private Label leftLabel;

    private ArrayList<BufferedWriter> engineOutputs = new ArrayList<>();
    private  ArrayList<Process> engineProcesses = new ArrayList<>();

    private int gameSize = 4;

    private Stage primaryStage;

    private EngineDisplay mainEngine;
    private int enginePlayDelay_ms = 10000;

    public void initialize() {

        startNewGame();

        initSidePanel();

    }

    public void setPrimaryStage( Stage stage ) {

        primaryStage = stage;

        primaryStage.setOnCloseRequest( e -> {
            for( Process engineProcess : engineProcesses ) {
                engineProcess.destroy();
                //System.out.println("killing process " + i);
            }
        });
    }

    public void initSidePanel() {

        Button butt = new Button("+");
        butt.setMaxWidth(3e8);

        butt.setOnAction( event -> {

            try {
                EngineDisplay eng = new EngineDisplay();
                int pos = sidePane.getChildren().size() - 1 ;

                sidePane.getChildren().add(pos,eng.getDropDown());
                eng.getDropDown().setExpanded(true);
                engineOutputs.add(eng.getOutputStream());
                engineProcesses.add(eng.getEngineProcess());

                eng.closeButton().setOnAction( click -> {
                    engineOutputs.remove( eng.getOutputStream() );
                    engineProcesses.remove( eng.getEngineProcess() );
                    sidePane.getChildren().remove(pos);
                    eng.kill();
                });

                eng.resetButton().setOnAction( click -> {
                    engineOutputs.remove( eng.getOutputStream() );
                    engineProcesses.remove( eng.getEngineProcess() );
                    eng.reset();
                    engineOutputs.add(eng.getOutputStream());
                    engineProcesses.add(eng.getEngineProcess());
                    updateEngineGameState(eng.getOutputStream());
                });

                updateEngineGameState(eng.getOutputStream());

                //main engine will always be first engine
                if( mainEngine == null ) {
                    mainEngine = eng;
                    mainEnginePlay.start();
                }

            } catch( Exception e ) {
                System.out.println(e.getMessage());
            }
        });

        sidePane.getChildren().add( butt );
    }

    private void updateEngineGameState( BufferedWriter engineOutput ) {
        //TODO complete update procedure

        try {
            engineOutput.write("newGame:" + gameSize);
            engineOutput.newLine();
            engineOutput.flush();
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

    public void startNewGame() {

        int gameSize;
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
                if( game == null ) {
                    Platform.exit();
                }
                break;
            }

            try {
                //parse int from user input
                gameSize = Integer.parseInt(result.get());

                if( gameSize <= 1 ) {
                    sizeInput = new TextInputDialog("7");
                    sizeInput.setHeaderText("");
                    sizeInput.setTitle("CubiCup Size Picker");
                    sizeInput.setContentText("Pick something more than 1 ...");
                } else {
                    this.gameSize = gameSize;
                    game = new CubiCup(gamePane,gameSize);
                    game.setEngineOutputs(engineOutputs);
                    game.setSpotHoverLabel(leftLabel);

                    //tell the engines a new game started
                    for( BufferedWriter engineOutput : engineOutputs ) {
                        engineOutput.write("newGame:" + gameSize );
                        engineOutput.newLine();
                        engineOutput.flush();
                    }

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

    public void reset() {
        game = new CubiCup(gamePane,gameSize);
        game.setEngineOutputs(engineOutputs);
        game.setSpotHoverLabel(leftLabel);


        //tell the engines a new game started
        for( BufferedWriter engineOutput : engineOutputs ) {
            try {
                engineOutput.write("newGame:" + gameSize);
                engineOutput.newLine();
                engineOutput.flush();
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public void exit() {
        Platform.exit();
    }

    Service<Void> mainEnginePlay = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {

            return new Task<Void>() {
                String line;

                @Override
                protected Void call() {

                    if( mainEngine != null ) {


                        try {

                            //initially wait for a bit
                            Thread.sleep(mainEngine.getThinkTime_ms());

                            while(true){

                                while( mainEngine.playingBlue() && game.turnIsGreen() ||
                                       mainEngine.playingGreen() && game.turnIsGreen() ) {
                                    Thread.sleep(10);
                                }

                                //other player turn over, think about move
                                Thread.sleep(mainEngine.getThinkTime_ms());

                                if( mainEngine.playingBlue() && game.turnIsBlue()
                                        || mainEngine.playingGreen() && game.turnIsGreen()
                                        || mainEngine.playingBoth() ) {
                                    //play move
                                    Platform.runLater(() -> {
                                        game.takeTurn(mainEngine.bestMove[0], mainEngine.bestMove[1], mainEngine.bestMove[2]);
                                    });
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    return null;
                }
            };
        }

        @Override
        protected void succeeded() {
            reset();
        }
    };

}