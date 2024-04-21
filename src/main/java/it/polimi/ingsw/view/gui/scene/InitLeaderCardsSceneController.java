package it.polimi.ingsw.view.gui.scene;

import it.polimi.ingsw.controller.actions.ActionParser;
import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.task.Task;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class implements the scene where the initial choice of the two leader cards is handled.
 */
public class InitLeaderCardsSceneController extends GenericScene {

    @FXML
    private AnchorPane rootpane;
    @FXML
    private Button lead1;
    @FXML
    private Button lead2;
    @FXML
    private Button lead3;
    @FXML
    private Button lead4;
    @FXML
    private Button doneButton;
    @FXML
    private ImageView leader1;
    @FXML
    private ImageView leader2;
    @FXML
    private ImageView leader3;
    @FXML
    private ImageView leader4;
    @FXML
    private Label errorLabel;

    private ActionParser requestBuilder = new ActionParser();
    private List<String> cards;
    int count = 0;
    JSONObject r1;
    JSONObject r2;
    String pathname;

    @FXML
    public void initialize() {
        super.setTitle("Choose 2 Leader Cards");

        pathname = "/images/cards/front/" + cards.get(0) + ".png";
        /* File file = new File(pathname);
        Image image = new Image(file.toURI().toString()); */
        Image image = new Image(pathname);
        leader1.setImage(image);

        pathname = "/images/cards/front/" + cards.get(1) + ".png";
        /* File file = new File(pathname);
        image = new Image(file.toURI().toString()); */
        image = new Image(pathname);
        leader2.setImage(image);

        pathname = "/images/cards/front/" + cards.get(2) + ".png";
        /* file = new File(pathname);
        image = new Image(file.toURI().toString()); */
        image = new Image(pathname);
        leader3.setImage(image);

        pathname = "/images/cards/front/" + cards.get(3) + ".png";
        /* file = new File(pathname);
        image = new Image(file.toURI().toString()); */
        image = new Image(pathname);
        leader4.setImage(image);

        lead1.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onLead1BtnClick);
        lead2.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onLead2BtnClick);
        lead3.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onLead3BtnClick);
        lead4.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onLead4BtnClick);
        doneButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onDoneBtnClick);
    }

    /**
     * This method loads the leader cards which the player must choose
     * @param cards leader cards list
     */
    public void setLoadedCards(List<String> cards) {
        Platform.runLater(() -> this.cards = cards);
    }

    public InitLeaderCardsSceneController(){
        super("initialLeadCards");
    }

    /**
     * This method builds the first request to be sent
     * @param i indicates which leader card to discard
     */
    private JSONObject buildRequest1(int i) {
        JSONObject request1 = requestBuilder.action(Actions.DISCARDLEADER)
                .leaderCard(cards.get(i))
                .buildRequest();
        return request1;
    }

    /**
     * This method builds the second request to be sent
     * @param i indicates which leader card to discard
     */
    private JSONObject buildRequest2(int i) {
        JSONObject request2 = requestBuilder.action(Actions.DISCARDLEADER)
                .leaderCard(cards.get(i))
                .buildRequest();
        return request2;
    }

    public void turnOffButtons() {
        Platform.runLater(() -> {
            lead1.setDisable(true);
            lead2.setDisable(true);
            lead3.setDisable(true);
            lead4.setDisable(true);
            doneButton.setDisable(true);
        });
    }

    public void turnOnButtons() {
        Platform.runLater(() -> {
            lead1.setDisable(false);
            lead2.setDisable(false);
            lead3.setDisable(false);
            lead4.setDisable(false);
            doneButton.setDisable(false);
        });
    }

    private void sendDiscardAction(JSONObject request1, JSONObject request2) {
        try {
            new Thread(() -> notify(new Task<>(client -> client.handleAction(request1)))).start();
            new Thread(() -> notify(new Task<>(client -> client.handleAction(request2)))).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        turnOffButtons();
    }

    /**
     * Handle the click on the first leader card button.
     * @param event the mouse click event.
     */
    private void onLead1BtnClick(Event event) {
        if (count == 0) {
            r1 = buildRequest1(0);
            count++;
            pathname = "/images/cards/back/LC.png";
            /* File file = new File(pathname);
            Image image = new Image(file.toURI().toString()); */
            Image image = new Image(pathname);
            leader1.setImage(image);
            lead1.setDisable(true);
        } else if (count == 1) {
            r2 = buildRequest2(0);
            count++;
            pathname = "/images/cards/back/LC.png";
            /* File file = new File(pathname);
            Image image = new Image(file.toURI().toString()); */
            Image image = new Image(pathname);
            leader1.setImage(image);
            lead1.setDisable(true);
        } else {
            errorLabel.setText("You can only discard 2 cards!");
        }
    }

    /**
     * Handle the click on the second leader card button.
     * @param event the mouse click event.
     */
    private void onLead2BtnClick(Event event) {
        if (count == 0) {
            r1 = buildRequest1(1);
            count++;
            pathname = "/images/cards/back/LC.png";
            /* File file = new File(pathname);
            Image image = new Image(file.toURI().toString()); */
            Image image = new Image(pathname);
            leader2.setImage(image);
            lead2.setDisable(true);
        } else if (count == 1) {
            r2 = buildRequest2(1);
            count++;
            pathname = "/images/cards/back/LC.png";
            /* File file = new File(pathname);
            Image image = new Image(file.toURI().toString()); */
            Image image = new Image(pathname);
            leader2.setImage(image);
            lead2.setDisable(true);
        } else {
            errorLabel.setText("You can only discard 2 cards!");
        }
    }

    /**
     * Handle the click on the third leader card button.
     * @param event the mouse click event.
     */
    private void onLead3BtnClick(Event event) {
        if (count == 0) {
            r1 = buildRequest1(2);
            count++;
            pathname = "/images/cards/back/LC.png";
            /* File file = new File(pathname);
            Image image = new Image(file.toURI().toString()); */
            Image image = new Image(pathname);
            leader3.setImage(image);
            lead3.setDisable(true);
        } else if (count == 1) {
            r2 = buildRequest2(2);
            count++;
            pathname = "/images/cards/back/LC.png";
            /* File file = new File(pathname);
            Image image = new Image(file.toURI().toString()); */
            Image image = new Image(pathname);
            leader3.setImage(image);
            lead3.setDisable(true);
        } else {
            errorLabel.setText("You can only discard 2 cards!");
        }
    }

    /**
     * Handle the click on the fourth leader card button.
     * @param event the mouse click event.
     */
    private void onLead4BtnClick(Event event) {
        if (count == 0) {
            r1 = buildRequest1(3);
            count++;
            pathname = "/images/cards/back/LC.png";
            /* File file = new File(pathname);
            Image image = new Image(file.toURI().toString()); */
            Image image = new Image(pathname);
            leader4.setImage(image);
            lead4.setDisable(true);
        } else if (count == 1) {
            r2 = buildRequest2(3);
            count++;
            pathname = "/images/cards/back/LC.png";
            /* File file = new File(pathname);
            Image image = new Image(file.toURI().toString()); */
            Image image = new Image(pathname);
            leader4.setImage(image);
            lead4.setDisable(true);
        } else {
            errorLabel.setText("You can only discard 2 cards!");
        }
    }

    /**
     * Handle the click on the done button.
     * @param event the mouse click event.
     */
    private void onDoneBtnClick(Event event) {
        sendDiscardAction(r1, r2);
    }

}
