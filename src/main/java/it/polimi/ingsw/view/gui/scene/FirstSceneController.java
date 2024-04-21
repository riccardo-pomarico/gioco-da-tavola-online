package it.polimi.ingsw.view.gui.scene;

import it.polimi.ingsw.network.ClientController;
import it.polimi.ingsw.network.ServerController;
import it.polimi.ingsw.observer.ViewObservable;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.view.gui.SceneController;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * This class implements the first scene shown.
 */
public class FirstSceneController extends GenericScene {
    private ClientController clientController;

    @FXML
    private AnchorPane rootpane;
    @FXML
    private Button playButton;
    @FXML
    private Button exitButton, singlePlayer;

    @FXML
    public void initialize() {
        super.setTitle("Welcome to Masters of Renaissance!");
        playButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onPlayBtnClick);
        exitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> System.exit(0));
        singlePlayer.addEventHandler(MouseEvent.MOUSE_CLICKED,this::singlePlayer);

    }

    public FirstSceneController(){
        super("welcome");
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    /**
     * Handle the click on the play button.
     * @param event the mouse click event.
     */
    private void onPlayBtnClick(Event event) {
        //SceneController.changeScene(event, "server_info_scene_controller.fxml");
        clientController.init();

    }

    private void singlePlayer(Event event){
        new Thread(() -> new ServerController(1330).startServer()).start();
        clientController.singlePlayerInit();
        ((PersonalDashboardSceneController) getScene("personalDashboard")).singlePlayerMode();
    }
}
