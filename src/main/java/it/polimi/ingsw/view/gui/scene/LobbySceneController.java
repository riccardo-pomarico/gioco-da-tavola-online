package it.polimi.ingsw.view.gui.scene;

import it.polimi.ingsw.network.ClientController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * This class implements the scene in which the lobby is shown.
 */
public class LobbySceneController extends GenericScene {

    @FXML
    private AnchorPane rootpane;
    @FXML
    private Label label;
    @FXML
    private Label playerJoined;
    @FXML
    private Label player1;
    @FXML
    private Label player2;
    @FXML
    private Label player3;
    @FXML
    private Label player4;

    private int num;
    private boolean initialized = false;
    private final String[] playerList = new String[]{" "," "," "," "};;
    private String newPlayer;

    @FXML
    public void initialize() {
        super.setTitle("Waiting for other players");
        label.setText("Waiting for other " + num + " players...");
        if(newPlayer!= null) {
            playerJoined.setText(newPlayer + " joined! ");
        }
        player1.setText(playerList[0]);
        player2.setText(playerList[1]);
        player3.setText(playerList[2]);
        player4.setText(playerList[3]);

        initialized = true;

    }

    public LobbySceneController(){
        super("lobby");
    }

    /**
     * This method sets the number of player to wait
     * @param num number of player to connect
     */
    public void setNumPlayers(int num) {
        Platform.runLater(() -> {
            this.num = num;
            if(initialized){
                label.setText("Waiting for other " + num + " players...");
            }
        });
    }

    public void newPlayer(String username){
        newPlayer = username;
    }

    public void otherPlayers(String[] usernames){
         for (int i = 0; i < usernames.length; i++){
                this.playerList[i] = usernames[i];
            }
    }


}