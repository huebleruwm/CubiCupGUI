package CubiCup;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
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

    private String[] cmd;

    public EngineDisplay() throws Exception {

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());

        if( !file.exists() ) {
            throw new Exception();
        }

        engineFile = file;

        dropDownName.setContent(engineOutput);

        reset.setOnAction( event -> reset() );

        buttonBox.getChildren().addAll(close,reset);
        engineOutput.getChildren().add( buttonBox );

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
            engineOutput.getChildren().add( buttonBox );

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
                                        engineOutput.getChildren().add(engineOutput.getChildren().size()-1,newText);
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
