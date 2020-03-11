package CubiCup;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

public abstract class Engine {

    private TitledPane dropDownName = new TitledPane();
    private VBox engineOutput = new VBox();

    private File engineFile;

    private ArrayList<String> valueNames = new ArrayList<>();
    private ArrayList<Text> values = new ArrayList<>();

    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String[] cmd;
    private Slider timeSlider = new Slider(1,10,5);

    protected HBox buttonBox = new HBox();
    private Button close = new Button("X");
    private Button reset = new Button("Reset");

    abstract public boolean isEngineTurn( int turn );

    String bestMove;

    public void initialize() throws Exception {

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());

        if( !file.exists() ) {
            throw new Exception();
        }

        engineFile = file;

        dropDownName.setContent(engineOutput);

        reset.setOnAction( event -> reset() );

        reset.focusTraversableProperty().setValue(false);

        buttonBox.getChildren().addAll(close,reset);

        timeSlider.setShowTickMarks(true);
        timeSlider.setShowTickLabels(true);

        engineOutput.getChildren().addAll( buttonBox, timeSlider );

        String engineFileName = engineFile.getName();

        if ( engineFileName.contains(".") && engineFileName.substring(engineFileName.lastIndexOf(".")).equals(".bash")) {
            cmd = new String[]{ "bash", engineFile.getPath() };
        } else if ( engineFileName.contains(".") && engineFileName.substring(engineFileName.lastIndexOf(".")).equals(".py")) {
            cmd = new String[]{ "python3", engineFile.getPath() };
        } else {
            cmd = new String[]{ engineFile.getPath() };
        }

        dropDownName.setText(engineFile.getName());

        process = Runtime.getRuntime().exec(cmd);

        reader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
        writer = new BufferedWriter( new OutputStreamWriter( process.getOutputStream() ) );

        runMain.start();

    }

    public Button closeButton() {
        return close;
    }

    public Button resetButton() {
        return reset;
    }

    public TitledPane getDropDown() {
        return dropDownName;
    }

    public int getThinkTime_ms() {
        return (int)(timeSlider.getValue() * 1000);
    }

    public void kill() {
        process.destroy();
    }

    public void output( String output ) {
        try {
            writer.write(output);
            writer.newLine();
            writer.flush();
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

    public void countDownToPlay( TurnBasedGame game ) throws Exception {

        long start = System.currentTimeMillis();
        long now = System.currentTimeMillis();

        try {
            while( isEngineTurn(game.getTurn()) && (now-start) < getThinkTime_ms() ) {
                double timeToMove =  (double)((int)(getThinkTime_ms() - (now-start)) / 100) / 10.0;
                Platform.runLater( () -> dropDownName.setText(engineFile.getName() + " -- " + timeToMove)  );
                now = System.currentTimeMillis();
                Thread.sleep(100);
            }
        } catch( Exception e ) {
            //e.printStackTrace();
            Platform.runLater( () -> dropDownName.setText(engineFile.getName())  );
            throw new Exception();
        }

        Platform.runLater( () -> dropDownName.setText(engineFile.getName())  );

    }

    public String getBestMove() {
        return bestMove;
    }

    public void reset() {

        process.destroy();

        try {

            process = Runtime.getRuntime().exec(cmd);

            valueNames.clear();
            values.clear();

            engineOutput.getChildren().clear();
            engineOutput.getChildren().addAll( buttonBox, timeSlider );

            reader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
            writer = new BufferedWriter( new OutputStreamWriter( process.getOutputStream() ) );

            runMain.restart();

        } catch( Exception e ) {
            System.out.println("Error resetting process for " + engineFile.getName() );
            e.printStackTrace();
        }
    }

    Service<Void> runMain = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {

            return new Task<Void>() {
                String line;

                @Override
                protected Void call() {
                    try {

                        while (process.isAlive()) {

                            line = reader.readLine();

                            if (line != null) {

                                String[] lineSplit = line.split(":");

                                if( lineSplit[0].equals("subscribe") ) {
                                    valueNames.add( lineSplit[1] );
                                    Text newText = new Text( lineSplit[1] + ": " );
                                    values.add( newText );
                                    Platform.runLater(() -> {
                                        engineOutput.getChildren().add(engineOutput.getChildren().size()-2,newText);
                                    });
                                } else {

                                    for (int i = 0; i < valueNames.size(); i++) {
                                        int counter = i;
                                        if (lineSplit[0].equals(valueNames.get(i))) {
                                            Platform.runLater(() -> {
                                                values.get(counter).setText(valueNames.get(counter) + ": " + lineSplit[1]);
                                            });
                                        }
                                    }
                                }

                                if( lineSplit[0].equals("Best Move") && lineSplit.length > 1 ) {
                                    bestMove = lineSplit[1];
                                }

                                if( lineSplit[0].equals("Error") ) {
                                    System.out.println(line);
                                }
                            }
                        }
                    } catch (IOException e) {
                        //e.printStackTrace();
                        System.out.println("Thread error/close interfacing with engine.");
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
