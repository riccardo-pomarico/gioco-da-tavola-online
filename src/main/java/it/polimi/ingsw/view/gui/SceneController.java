package it.polimi.ingsw.view.gui;

import com.sun.javafx.scene.control.IntegerField;
import it.polimi.ingsw.controller.actions.ActionParser;
import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.network.ClientController;
import it.polimi.ingsw.observer.ViewObservable;
import it.polimi.ingsw.observer.ViewObserver;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.view.gui.scene.*;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;

public class SceneController extends ViewObservable {
    private Stage stage;
    private Map<String, Map.Entry<String,GenericScene>> sceneMap;
    private ViewObserver viewObserver;
    private Button confirmBtn;
    private TextField textField;
    private TextField shelfField;
    private TextField depositField;
    private Label errorLabel = new Label(" ");
    private List<String> playerLeadCardDeck = new ArrayList<>();
    private Stage dialog = new Stage();
    private String marble;
    String res;
    private Parent currentParent;

    public SceneController(){
        this.sceneMap = new HashMap<>();
        this.stage = new Stage();
        stage.setResizable(false);
        stage.setMaximized(true);
        initScenes();
    }

    private void initScenes() {
        sceneMap.put("welcome",new AbstractMap.SimpleEntry<>("first_scene_controller.fxml",new FirstSceneController()));
        sceneMap.put("serverInfo", new AbstractMap.SimpleEntry<>("server_info_scene_controller.fxml",new ServerInfoSceneController()));
        sceneMap.put("lobby", new AbstractMap.SimpleEntry<>("lobby_scene_controller.fxml",new LobbySceneController()));
        sceneMap.put("numPlayers",new AbstractMap.SimpleEntry<>("num_players_scene_controller.fxml",new NumPlayersSceneController()));
        sceneMap.put("personalDashboard",new AbstractMap.SimpleEntry<>("personal_dashboard_scene_controller.fxml",new PersonalDashboardSceneController()));
        sceneMap.put("market",new AbstractMap.SimpleEntry<>("market_scene_controller.fxml",new MarketSceneController()));
        sceneMap.put("buyDC", new AbstractMap.SimpleEntry<>("buy_dc_scene_controller.fxml", new BuyDCSceneController()));
        sceneMap.put("activateLC", new AbstractMap.SimpleEntry<>("activate_lc_scene_controller.fxml", new ActivateLCSceneController()));
        sceneMap.put("initialLeadCards", new AbstractMap.SimpleEntry<>("init_leader_cards_scene_controller.fxml", new InitLeaderCardsSceneController()));
        sceneMap.put("depositResource", new AbstractMap.SimpleEntry<>("deposit_resource_scene_controller.fxml", new DepositResourceSceneController()));
        sceneMap.put("moveResources", new AbstractMap.SimpleEntry<>("move_resources_scene_controller.fxml", new MoveResourcesSceneController()));
        sceneMap.forEach((id,entry) -> entry.getValue().setSceneController(this));
    }

    public void setViewObserver(ViewObserver observer){
        this.viewObserver = observer;
        try {
            addViewObserver(observer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sceneMap.forEach((id,entry) -> entry.getValue().addViewObserver(observer));
    }

    /**
     * This method changes the current scene
     * @param sceneId indicates the new scene
     * @throws IllegalArgumentException
     */
    public void changeScene(String sceneId) throws IllegalArgumentException {
        GenericScene scene = getScene(sceneId);
        FXMLLoader loader = new FXMLLoader(SceneController.class.getResource("/fxml/" + sceneMap.get(sceneId).getKey()));
        Parent rootLayout = null;
        loader.setController(getScene(sceneId));

        try {
            rootLayout = loader.load();
            rootLayout.setId(sceneId);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        currentParent = rootLayout;
        Scene newScene = new Scene(rootLayout);
        stage.setScene(newScene);
        stage.setTitle(scene.getTitle());
        stage.show();
    }

    /**
     * This method loads a new scene in background
     * @param sceneId indicates the new scene
     * @throws IllegalArgumentException
     */
    public void loadScene(String sceneId) throws IllegalArgumentException {
        GenericScene scene = getScene(sceneId);
        FXMLLoader loader = new FXMLLoader(SceneController.class.getResource("/fxml/" + sceneMap.get(sceneId).getKey()));
        Parent rootLayout = null;
        loader.setController(getScene(sceneId));

        try {
            rootLayout = loader.load();
            rootLayout.setId(sceneId);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        currentParent = rootLayout;

        if (stage.getScene().getRoot().getId().equals(rootLayout.getId())) {
            Scene newScene = new Scene(rootLayout);
            stage.setScene(newScene);
            stage.setTitle(scene.getTitle());
            stage.show();
        }
    }

    /**
     * This method loads the first scene
     * @param clientController
     */
    public void changeSceneInit(ClientController clientController) {
        FXMLLoader loader = new FXMLLoader(SceneController.class.getResource("/fxml/" + sceneMap.get("welcome").getKey()));
        Parent rootLayout = null;
        FirstSceneController scene = (FirstSceneController) getScene("welcome");
        loader.setController(scene);
        scene.setClientController(clientController);

        try {
            rootLayout = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        //c = loader.getController();

        scene.addViewObserver(clientController);

        Scene newScene = new Scene(rootLayout);
        stage.setScene(newScene);
        stage.setTitle(scene.getTitle());
        stage.show();
    }

    /**
     * This method return a scene
     * @param scene indicates the required scene
     * @throws IllegalArgumentException
     */
    public GenericScene getScene(String scene) throws IllegalArgumentException{
        try{
            return sceneMap.get(scene).getValue();
        }catch (ClassCastException | NullPointerException e){
            throw new IllegalArgumentException("no such scene as "+scene);
        }
    }

    /**
     * This method creates a popup in case of a request for a black marble
     */
    public void blankMarblePopUp() {
        VBox dialogVbox = new VBox(20);

        dialogVbox.getChildren().add(new Text("You have received a black marble!"));
        dialogVbox.getChildren().add(new Text("Choose a resource to deposit"));
        dialogVbox.getChildren().add(new Text("[Shield, Coin, Stone, Servant]"));
        textField = new TextField();
        dialogVbox.getChildren().add(textField);

        dialogVbox.getChildren().add(new Text("Which shelf would you like to deposit the resource in?"));
        shelfField = new TextField();
        dialogVbox.getChildren().add(shelfField);

        confirmBtn = new Button("CONFIRM");
        dialogVbox.getChildren().add(confirmBtn);

        Scene dialogScene = new Scene(dialogVbox, 300, 300);
        dialog.setScene(dialogScene);
        dialog.show();

        confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onConfirmBlankMarbleBtnClick);

    }

    /**
     * Handle the click on the confirm button.
     * @param event the mouse click event.
     */
    private void onConfirmBlankMarbleBtnClick (Event event) {
        String text = textField.getText();
        int shelf = Integer.parseInt(shelfField.getText());
        shelf--;
        res = new Marble(Resource.valueOf(text.toUpperCase())).generateId();

        if (res.charAt(0) == 'R') {
            ActionParser requestBuilder = new ActionParser();
            try {
                JSONObject request = requestBuilder
                        .action(Actions.DEPOSIT)
                        .marble(res)
                        .targetDeposit(2)
                        .shelf(shelf)
                        .blackMarble(true)
                        .buildRequest();
                new Thread(() ->  notify(new Task<>(viewObserver -> viewObserver.handleAction(request)))).start();
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        } else {
            // Non funziona correttamente l'error label
            errorLabel.setText("Please, insert the correct resource");
            blankMarblePopUp();
        }

        dialog.close();
    }

    /**
     * This method creates a popup in case of a request for a regular marble
     * @param marble indicates which resource you can deposit
     * @param targetDeposit indicates where the resource will be deposited
     */
    public void resourcesPopUp (String marble, int targetDeposit) {

        VBox dialogVbox = new VBox(20);

        this.marble = marble;

        dialogVbox.getChildren().add(new Text("You have received a "+ new Marble(marble).getType() +" marble!"));
        dialogVbox.getChildren().add(new Text("Which shelf would you like to deposit the resource in?"));
        shelfField = new TextField();
        dialogVbox.getChildren().add(shelfField);

        confirmBtn = new Button("CONFIRM");
        dialogVbox.getChildren().add(confirmBtn);

        Scene dialogScene = new Scene(dialogVbox, 400, 300);
        dialog.setScene(dialogScene);
        dialog.show();

        confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onConfirmWarehouseBtnClick);

    }

    /**
     * This method creates a popup in case of a request for a regular marble
     * @param marble indicates which resource you can deposit
     */
    public void resourcesPopUp (String marble) {

        VBox dialogVbox = new VBox(20);

        this.marble = marble;

        dialogVbox.getChildren().add(new Text("You have received a "+ new Marble(marble).getType() +" marble!"));
        dialogVbox.getChildren().add(new Text("Where would you like to deposit it? [1 = Strong Box, 2 = Warehouse]"));

        depositField = new TextField();
        dialogVbox.getChildren().add(depositField);

        confirmBtn = new Button("CONFIRM");
        dialogVbox.getChildren().add(confirmBtn);

        Scene dialogScene = new Scene(dialogVbox, 400, 300);
        dialog.setScene(dialogScene);
        dialog.show();

        confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onConfirmResourceBtnClick);

    }

    /**
     * Handle the click on the confirm button.
     * @param event the mouse click event.
     */
    private void onConfirmResourceBtnClick(Event event) {
        int targetDeposit = Integer.parseInt(depositField.getText());

        if (targetDeposit == 2) {

            dialog.close();

            VBox dialogVbox = new VBox(20);

            dialogVbox.getChildren().add(new Text("Which shelf would you like to deposit the resource in?"));
            shelfField = new TextField();
            dialogVbox.getChildren().add(shelfField);

            confirmBtn = new Button("CONFIRM");
            dialogVbox.getChildren().add(confirmBtn);

            Scene dialogScene = new Scene(dialogVbox, 300, 300);
            dialog.setScene(dialogScene);
            dialog.show();

            confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onConfirmWarehouseBtnClick);
        } else {
            try {
                ActionParser requestBuilder = new ActionParser();
                JSONObject request = requestBuilder
                        .action(Actions.DEPOSIT)
                        .marble(marble)
                        .targetDeposit(targetDeposit)
                        .shelf(0)
                        .blackMarble(false)
                        .buildRequest();
                notify(new Task<>(viewObserver -> viewObserver.handleAction(request)));
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }

        dialog.close();
    }

    /**
     * Handle the click on the confirm button indicating which shelf in the warehouse to use.
     * @param event the mouse click event.
     */
    private void onConfirmWarehouseBtnClick(Event event) {
        int shelf = Integer.parseInt(shelfField.getText());
        shelf--;

        ActionParser requestBuilder = new ActionParser();
        try {
            JSONObject request = requestBuilder
                    .action(Actions.DEPOSIT)
                    .marble(marble)
                    .targetDeposit(2)
                    .shelf(shelf)
                    .blackMarble(false)
                    .buildRequest();
            new Thread(() ->  notify(new Task<>(viewObserver -> viewObserver.handleAction(request)))).start();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        dialog.close();
    }

    /**
     * This method creates a popup for things you can do at the end of the turn
     * @param playerLeadCardDeck indicates the leader cards owned by the player
     */
    public void afterActionPopUp (List<String> playerLeadCardDeck) {
        this.playerLeadCardDeck = playerLeadCardDeck;

        VBox dialogVbox = new VBox(20);

        //dialog.initModality(Modality.APPLICATION_MODAL);

        dialog.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.runLater(() -> afterActionPopUp(playerLeadCardDeck));
            }
        });

        dialogVbox.getChildren().add(new Text("Now that you have executed your action, choose how you want to proceed."));

        Button warehouseBtn = new Button("Move your resources in the warehouse");
        dialogVbox.getChildren().add(warehouseBtn);

        Button leaderBtn = new Button("Activate a leader card");
        dialogVbox.getChildren().add(leaderBtn);

        Button turnBtn = new Button("Finish your turn");
        dialogVbox.getChildren().add(turnBtn);

        Scene dialogScene = new Scene(dialogVbox, 500, 200);
        dialog.setScene(dialogScene);
        dialog.show();

        warehouseBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onWarehouseBtnClick);
        leaderBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onLeaderBtnClick);
        turnBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onTurnBtnClick);
    }

    /**
     * Handle the click on the 'move resources' button.
     * @param event the mouse click event.
     */
    private void onWarehouseBtnClick(Event event) {
        dialog.close();
        changeScene("moveResources");
    }

    /**
     * Handle the click on the 'activate a leader card' button.
     * @param event the mouse click event.
     */
    private void onLeaderBtnClick(Event event) {
        dialog.close();
        ActivateLCSceneController c = (ActivateLCSceneController) getScene("activateLC");
        c.setLoadedCards(playerLeadCardDeck);
        changeScene("activateLC");
    }

    /**
     * Handle the click on the 'finish your turn' button.
     * @param event the mouse click event.
     */


    private void onTurnBtnClick(Event event) {
        dialog.close();
        PersonalDashboardSceneController c = (PersonalDashboardSceneController) getScene("personalDashboard");
        c.turnOffButtons();
        new Thread(() -> {
            notify(new Task<>(ViewObserver::unblockIncomingTasks));
            notify(new Task<>(ViewObserver::changeOfTurn));
        }).start();
        changeScene("personalDashboard");
    }
    /**
     * This method creates a popup which indicates the order of turns during the game
     * @param players
     * @param username
     */
    public void playerOrderPopUp(String[] players, String username) {
        Stage dialog = new Stage();
        String message;

        dialog.initOwner(stage);
        VBox dialogVbox = new VBox(20);

        for(int i = 0; i < players.length; i++){
            switch (i) {
                case 0:
                    message = "- "  + players[i] + " is the first player.";
                    break;
                case 1:
                    message = "- "  + players[i] + " is the second player.\n  They will be given a Resource of their choice. " ;
                    break;
                case 2:
                    message = "- "  + players[i] + " is the third player.\n  They will be given a Resource of their choice and a Faith Point. " ;
                    break;
                case 3:
                    message = "- "  + players[i] + " is the fourth player.\n  They will be given two Resources of their choice and a Faith Point. " ;
                    break;
                default:
                    message = "";
                    break;
            }

            if(players[i].equals(username)){
                message = message.replace(players[i],"You");
                message = message.replace("They", "You");
                message = message.replace("is", "are");
                message = message.replace("their", "your");}

            dialogVbox.getChildren().add(new Text(message));
        }

        Scene dialogScene = new Scene(dialogVbox, 300, 300);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    /**
     * This method creates a popup which contains a message
     * @param type
     * @param message
     */
    public void notificationPopUp(String type, String message) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text(message));
        Scene dialogScene = new Scene(dialogVbox, 500, 100);
        dialog.setScene(dialogScene);
        dialog.show();
    }

}
