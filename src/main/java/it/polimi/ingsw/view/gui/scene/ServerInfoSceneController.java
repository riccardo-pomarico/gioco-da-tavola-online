package it.polimi.ingsw.view.gui.scene;

import com.sun.javafx.scene.control.IntegerField;
import it.polimi.ingsw.observer.ViewObserver;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.view.InputHandler;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * This class implements the scene where users connect to the server and insert the username.
 */
public class ServerInfoSceneController extends GenericScene {

    @FXML
    private AnchorPane rootpane;
    @FXML
    private TextField nicknameField, newNickname;
    @FXML
    private TextField addressField;
    @FXML
    private IntegerField portField;
    @FXML
    private Button connectButton, reconnectYes, reconnectNo;
    @FXML
    private Label errorLabel, addressLabel, portLabel, nicknameLabel, reconnectLabel1, reconnectLabel2, reconnectLabel3;


    @FXML
    public void initialize() {
        super.setTitle("Connection information");
        connectButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onConnectBtnClick);
        reconnectNo.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> reconnectChoiceBtnClick(false));
        reconnectYes.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> reconnectChoiceBtnClick(true));

    }

    public ServerInfoSceneController(){
        super("serverInfo");
    }

    public void setErrorLabel(String error){
        errorLabel.setText(error);
    }

    public void reconnect(){
        addressLabel.setVisible(false);
        portLabel.setVisible(false);
        nicknameLabel.setVisible(false);
        portField.setVisible(false);
        addressField.setVisible(false);
        nicknameField.setVisible(false);
        reconnectLabel1.setVisible(true);
        reconnectLabel2.setVisible(true);
        reconnectYes.setVisible(true);
        reconnectNo.setVisible(true);
    }

    private void reconnectChoiceBtnClick(boolean reconnect){
        reconnectYes.setDisable(true);
        reconnectNo.setDisable(true);
        if(reconnect){
            new Thread(() -> notify(new Task<>(ViewObserver::reconnectToServer))).start();
            ((PersonalDashboardSceneController) getScene("personalDashboard")).turnOffButtons();
            changeScene("personalDashboard");
        }else{
            reconnectLabel3.setVisible(true);
            newNickname.setVisible(true);
            connectButton.removeEventHandler(MouseEvent.MOUSE_CLICKED, this::onConnectBtnClick);
            connectButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onReconnectBtnClick);
        }

    }

    private void onReconnectBtnClick(Event event){
        String nickname = newNickname.getText();
        if(!InputHandler.checkUsername(nickname)){
            setErrorLabel(InputHandler.getUsernameError());
        }else{
            new Thread(() -> notify(new Task<>(cc -> cc.logIn(nickname)))).start();
        }
    }
    private void onConnectBtnClick(Event event) {
        String address = addressField.getText();
        if(!InputHandler.checkIPAddress(address)){
            setErrorLabel(InputHandler.getServerAddressError());
            return;
        }
        int port = portField.getValue();
        if(!InputHandler.checkPort(port)){
            setErrorLabel(InputHandler.getPortError());
            return;
        }
        String nickname = nicknameField.getText();
        if(!InputHandler.checkUsername(nickname)){
            setErrorLabel(InputHandler.getUsernameError());
            return;
        }
        new Thread(() -> notify(new Task<>(cc -> cc.playerInfo(new String[] {nickname, address,Integer.toString(port)})))).start();
    }
}
