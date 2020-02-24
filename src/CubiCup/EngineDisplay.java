package CubiCup;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

public class EngineDisplay {

    private TitledPane dropDownName = new TitledPane();

    private VBox engineOutput = new VBox();

    private ArrayList<String> valueNames = new ArrayList<String>();
    private ArrayList<Text> values = new ArrayList<Text>();

    private Process process;
    private BufferedReader reader;
    private BufferedWriter output;

    private File engineFile;

    private HBox buttonBox = new HBox();
    private Button close = new Button("X");
    private Button reset = new Button("Reset");

    private ToggleGroup playGroup = new ToggleGroup();
    private ToggleButton playBlue = new ToggleButton("Blue");
    private ToggleButton playGreen = new ToggleButton("Green");
    private ToggleButton playBoth = new ToggleButton("Both");

    private Slider timeSlider = new Slider(1,10,5);

    private String[] cmd;

    int[] bestMove = {0,0,0};

    public EngineDisplay() throws Exception {

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());

        if( !file.exists() ) {
            throw new Exception();
        }

        engineFile = file;

        dropDownName.setContent(engineOutput);

        reset.setOnAction( event -> reset() );

        playBlue.setToggleGroup(playGroup);
        playGreen.setToggleGroup(playGroup);
        playBoth.setToggleGroup(playGroup);

        reset.focusTraversableProperty().setValue(false);
        playBlue.focusTraversableProperty().setValue(false);
        playGreen.focusTraversableProperty().setValue(false);
        playBoth.focusTraversableProperty().setValue(false);

        buttonBox.getChildren().addAll(close,reset,playBlue,playGreen,playBoth);

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
        output = new BufferedWriter( new OutputStreamWriter( process.getOutputStream() ) );

        runMain.start();
    }

    public TitledPane getDropDown() {
        return dropDownName;
    }

    public BufferedWriter getOutputStream() {
        return output;
    }

    public Process getEngineProcess() {
        return process;
    }

    public Button closeButton() {
        return close;
    }

    public Button resetButton() {
        return reset;
    }

    public boolean playingBlue() {
        return playBlue.isSelected();
    }

    public boolean playingGreen() {
        return playGreen.isSelected();
    }

    public boolean playingBoth() {
        return playBoth.isSelected();
    }

    public int getThinkTime_ms() {
        return (int)(timeSlider.getValue() * 1000);
    }

    public void kill() {
        process.destroy();
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
            output = new BufferedWriter( new OutputStreamWriter( process.getOutputStream() ) );

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

                                //System.out.println(lineSplit[0]);
                                //System.out.println(lineSplit[1]);

                                if( lineSplit[0].equals("subscribe") ) {
                                    valueNames.add( lineSplit[1] );
                                    Text newText = new Text( lineSplit[1] + ": " );
                                    values.add( newText );
                                    Platform.runLater(() -> {
                                        engineOutput.getChildren().add(engineOutput.getChildren().size()-2,newText);
                                    });
                                }

                                for (int i = 0; i < valueNames.size(); i++) {
                                    int counter = i;
                                    if (lineSplit[0].equals(valueNames.get(i))) {
                                        Platform.runLater(() -> {
                                            values.get(counter).setText(valueNames.get(counter) + ": " + lineSplit[1]);
                                        });
                                    }
                                }

                                if( lineSplit[0].equals("Best Move") && lineSplit.length > 1 ) {
                                    String[] coords = lineSplit[1].split(",");
                                    bestMove[0] = Integer.parseInt(coords[0].replaceAll("[ (]",""));
                                    bestMove[1] = Integer.parseInt(coords[1].replace(" ",""));
                                    bestMove[2] = Integer.parseInt(coords[2].replaceAll("[ )]",""));
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
