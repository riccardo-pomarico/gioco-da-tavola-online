package it.polimi.ingsw.view.gui.scene;

import it.polimi.ingsw.observer.ViewObserver;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.view.InputHandler;
import it.polimi.ingsw.view.gui.SceneController;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * This class implements the scene where the number of players is set.
 */
public class NumPlayersSceneController extends GenericScene {

    @FXML
    private TextField numberOfPlayersField;
    @FXML
    private Button confirmButton;
    @FXML
    private Label label;

    @FXML
    public void initialize() {
        super.setTitle("Select the number of players");
        confirmButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onConfirmBtnClick);
    }

    public NumPlayersSceneController(){
        super("numPlayers");
    }

    /**
     * Handle the click on the confirm button.
     * @param event the mouse click event.
     */
    private void onConfirmBtnClick(Event event) {
        int numPlayers = Integer.parseInt(numberOfPlayersField.getText());
        if (!InputHandler.checkNumPlayers(numPlayers)){
            label.setText(InputHandler.getNumPlayersError());
        }else {
            if (numPlayers == 1) {
                new Thread(() -> notify(new Task<>( cc -> {
                        cc.setNumberOfPlayers(1);
                        cc.startGame();
                }))).start();
                PersonalDashboardSceneController c = (PersonalDashboardSceneController) getScene("personalDashboard");
                c.singlePlayerMode();
            } else {
                label.setText(" ");
                new Thread(() -> notify(new Task<>(cc -> cc.setNumberOfPlayers(numPlayers)))).start();
                Platform.runLater(() -> {
                    LobbySceneController c = (LobbySceneController) getScene("lobby");
                    c.setNumPlayers(numPlayers - 1);
                    changeScene("lobby");
                });
            }
        }
    }
}
