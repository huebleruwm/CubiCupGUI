package CubiCup;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Controller {

    @FXML private Label leftLabel;
    @FXML private MenuBar topMenu;
    @FXML private TabPane games;

    private ArrayList<GameController> gameControllers = new ArrayList<>();

    private Stage primaryStage;


    public void initialize() {
        startNewGameTab();
    }

    public void setPrimaryStage( Stage stage ) {

        primaryStage = stage;

        primaryStage.setOnCloseRequest( e -> {
            for( GameController gameController : gameControllers ) {
                gameController.close();
            }
        });
    }

    public void exit() {
        Platform.exit();
    }

    public void startNewGameTab() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameWindow.fxml"));
        try {
            Tab newTab = new Tab("Unspecified");
            newTab.setContent( loader.load() );
            GameController gameController = ((GameController)loader.getController());

            games.getTabs().add( newTab );

            gameController.setTab(newTab);
            gameController.setLeftLabel(leftLabel);
            gameControllers.add(loader.getController());

            newTab.setOnCloseRequest( event -> {
                games.getTabs().remove(newTab);
                gameController.close();
                gameControllers.remove(gameController);
            });

            gameController.setMenu(topMenu);
            games.getSelectionModel().select(newTab);

        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

}