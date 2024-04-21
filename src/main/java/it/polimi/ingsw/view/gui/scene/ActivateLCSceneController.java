package it.polimi.ingsw.view.gui.scene;

import com.sun.javafx.scene.control.IntegerField;
import it.polimi.ingsw.controller.actions.ActionParser;
import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.model.cards.BonusType;
import it.polimi.ingsw.model.cards.LeaderCard;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.observer.ViewObserver;
import it.polimi.ingsw.task.Task;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the scene showing leader cards that can be activated.
 */
public class ActivateLCSceneController extends GenericScene {

    @FXML
    private AnchorPane rootpane;
    @FXML
    private Button backButton;
    @FXML
    private Button leader1Button;
    @FXML
    private Button leader2Button;
    @FXML
    private ImageView leader1;
    @FXML
    private ImageView leader2;

    private List<String> cards = new ArrayList<>();
    private ActionParser requestBuilder = new ActionParser();
    private Stage dialog;
    String pathname;

    @FXML
    public void initialize() {
        super.setTitle("Activate Leader Card");

        pathname = "/images/cards/front/" + cards.get(0) + ".png";
        /* File file = new File(pathname);
        Image image = new Image(file.toURI().toString()); */
        Image image = new Image(pathname);
        leader1.setImage(image);

        pathname = "/images/cards/front/" + cards.get(1) + ".png";
        /* File file = new File(pathname);
           Image image = new Image(file.toURI().toString()); */
        image = new Image(pathname);
        leader2.setImage(image);

        backButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onBackBtnClick);
        leader1Button.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onLeader1BtnClick);
        leader2Button.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onLeader2BtnClick);
    }

    public ActivateLCSceneController(){
        super("activateLC");
    }

    /**
     * This method loads the player's leader cards
     * @param cards leader cards list
     */
    public void setLoadedCards(List<String> cards) {
        this.cards = cards;
    }

    /**
     * Handle the click on the back button.
     * @param event the mouse click event.
     */
    private void onBackBtnClick(Event event) {
        PersonalDashboardSceneController c = (PersonalDashboardSceneController) getScene("personalDashboard");
        c.turnOffButtons();
        changeScene("personalDashboard");
        new Thread(() -> notify(new Task<>(ViewObserver::afterAction))).start();
    }

    /**
     * Handle the click on the first leader card button.
     * @param event the mouse click event.
     */
    private void onLeader1BtnClick(Event event) {
        CardSet cardSet = new CardSet();
        cardSet.loadCards();

        try {
            LeaderCard card1 = (LeaderCard) cardSet.findCard(cards.get(0));

            if (card1.getBonusType() == BonusType.PRODUCTIONRULE) {
                PersonalDashboardSceneController c = (PersonalDashboardSceneController) getScene("personalDashboard");
                c.setBonus1(card1.getBonusRule());
                loadScene("personalDashboard");
            }
            JSONObject request = requestBuilder.action(Actions.ACTIVATELEADER).leaderCard(cards.get(0)).buildRequest();
            new Thread(() -> notify(new Task<>(client -> client.handleAction(request)))).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle the click on the second leader card button.
     * @param event the mouse click event.
     */
    private void onLeader2BtnClick(Event event) {
        CardSet cardSet = new CardSet();
        cardSet.loadCards();

        try {
            LeaderCard card2 = (LeaderCard) cardSet.findCard(cards.get(1));

            if (card2.getBonusType() == BonusType.PRODUCTIONRULE) {
                PersonalDashboardSceneController c = (PersonalDashboardSceneController) getScene("personalDashboard");
                c.setBonus2(card2.getBonusRule());
                loadScene("personalDashboard");
            }
            JSONObject request = requestBuilder.action(Actions.ACTIVATELEADER).leaderCard(cards.get(1)).buildRequest();
            new Thread(() -> notify(new Task<>(client -> client.handleAction(request)))).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}