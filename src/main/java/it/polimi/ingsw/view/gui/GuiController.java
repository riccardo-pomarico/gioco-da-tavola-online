package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.network.ClientController;
import it.polimi.ingsw.view.gui.scene.FirstSceneController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GuiController extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneController sceneController = new SceneController();
        Gui view = new Gui(sceneController);
        ClientController clientController = new ClientController(view);
        view.addViewObserver(clientController);
        sceneController.changeSceneInit(clientController);
    }
}
