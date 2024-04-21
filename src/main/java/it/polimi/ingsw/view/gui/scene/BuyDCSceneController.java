package it.polimi.ingsw.view.gui.scene;

import com.sun.javafx.scene.control.IntegerField;
import it.polimi.ingsw.controller.actions.ActionParser;
import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.cards.Color;
import it.polimi.ingsw.model.cards.DevelopmentCard;
import it.polimi.ingsw.model.cards.Level;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.network.ClientController;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.view.InputHandler;
import it.polimi.ingsw.view.gui.SceneController;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This class implements the scene showing the development cards available for purchase.
 */
public class BuyDCSceneController extends GenericScene {

    @FXML
    private AnchorPane rootpane;
    @FXML
    private Button buyButton, back_button;
    @FXML
    private ImageView c0_0,c0_1,c0_2,c0_3,c1_0,c1_1,c1_2,c1_3,c2_0,c2_1,c2_2,c2_3, magnified_card;
    @FXML
    private Label errorLabel, selectLabel, selected_label;

    private List<String> cards = new ArrayList<>();
    private int i;
    private ActionParser requestBuilder = new ActionParser();
    private Button confirmBtn;
    private TextField depositField;
    private TextField slotField;
    private List<String>[][] organizedCardset = new List[Level.values().length][Color.values().length];
    private CardSet cardSet;
    private String selectedCard;
    Stage dialog = new Stage();


    @FXML
    public void initialize() {
        super.setTitle("Buy Development Card");
        ImageView[][] cards = new ImageView[][] { {c0_0,c0_1,c0_2,c0_3},{c1_0,c1_1,c1_2,c1_3},{c2_0,c2_1,c2_2,c2_3}};
        String pathname, cardId;
        Image image;
        buyButton.addEventHandler(MouseEvent.MOUSE_CLICKED,this::onBuyBtnClick);
        back_button.addEventHandler(MouseEvent.MOUSE_CLICKED,this::onBackButtonClick);
        magnified_card.setVisible(false);
        buyButton.setVisible(false);
        selected_label.setVisible(false);
        for(int i = 0; i < Level.values().length; i++){
            for (int j = 0; j < Color.values().length; j++){

                try {
                    cardId = organizedCardset[i][j].get(0);
                    String finalCardId = cardId;
                    cards[i][j].addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> selectCard(finalCardId));
                    pathname = "/images/cards/front/" + cardId + ".png";
                    image = new Image(pathname);
                    cards[i][j].setImage(image);
                } catch (IndexOutOfBoundsException e) {

                }

            }
        }
    }

    /**
     * This method shows the card selected
     * @param cardId indicates the card selected
     */
    private void selectCard(String cardId){
        magnified_card.setImage(new Image("/images/cards/front/" + cardId + ".png"));
        selectedCard = cardId;
        selectLabel.setVisible(false);
        magnified_card.setVisible(true);
        buyButton.setVisible(true);
        selected_label.setVisible(true);
    }

    public BuyDCSceneController() { super("buyDC"); }

    /**
     * This method loads the available development cards
     * @param cardSet development cards list
     */
    public void setLoadedCards(CardSet cardSet) {
        cardSet.getDevCardList().stream().filter(c -> c.generateId().charAt(0) != 'L').collect(Collectors.toList()).forEach(dc -> {
            cards.add(dc.generateId());
        });
    }

    public void updateCardset(List<String>[][] organizedCardset, CardSet cardSet){
        this.organizedCardset = organizedCardset;
        this.cardSet = cardSet;
    }

    /**
     * Handle the click on the buy button.
     * @param event the mouse click event.
     */
    private void onBuyBtnClick(Event event) {

        VBox dialogVbox = new VBox(20);

        dialogVbox.getChildren().add(new Text("Where do you want to withdraw your resources from?"));
        dialogVbox.getChildren().add(new Text("[1 = Strong Box, 2 = warehouse, 3 = auto]"));
        depositField = new TextField();
        dialogVbox.getChildren().add(depositField);

        dialogVbox.getChildren().add(new Text("Which slot do you want to store card?"));
        dialogVbox.getChildren().add(new Text("[1, 2, 3]"));
        slotField = new TextField();
        dialogVbox.getChildren().add(slotField);

        confirmBtn = new Button("CONFIRM");
        dialogVbox.getChildren().add(confirmBtn);
        Scene dialogScene = new Scene(dialogVbox, 300, 300);
        dialog.setScene(dialogScene);
        dialog.show();

        confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onConfirmBtnClick);

    }

    /**
     * Handle the click on the back button.
     * @param event the mouse click event.
     */
    private void onBackButtonClick(Event event){
        Platform.runLater(() -> {
            ((PersonalDashboardSceneController) getScene("personalDashboard")).turnOnButtons();
        });
        changeScene("personalDashboard");
    }

    /**
     * Handle the click on the confirm button.
     * @param event the mouse click event.
     */
    private void onConfirmBtnClick(Event event) {
        try {
            JSONObject request = requestBuilder
                    .action(Actions.DEVCARDPURCHASE)
                    .developmentCard(selectedCard)
                    .targetDeposit(Integer.parseInt(depositField.getText()))
                    .slot(Integer.parseInt(slotField.getText()) - 1)
                    .buildRequest();
            new Thread(() -> notify(new Task<>(c -> c.handleAction(request)))).start();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        dialog.close();
    }


}
