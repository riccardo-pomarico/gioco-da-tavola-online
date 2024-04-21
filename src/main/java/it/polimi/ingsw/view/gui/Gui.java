package it.polimi.ingsw.view.gui;

import com.sun.javafx.scene.control.IntegerField;
import it.polimi.ingsw.controller.actions.ActionParser;
import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.model.cards.Color;
import it.polimi.ingsw.model.cards.DevelopmentCard;
import it.polimi.ingsw.model.cards.Level;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.observer.ViewObserver;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.observer.ViewObservable;
import it.polimi.ingsw.view.View;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import it.polimi.ingsw.view.gui.scene.*;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Gui extends ViewObservable implements View {
    private SceneController sceneController;
    private final CardSet genericCardSet = new CardSet();
    private Task<View> lastTask;
    private String username;
    private ActionParser requestBuilder = new ActionParser();
    private List<String>[] slots = new List[3];
    private List<Resource>[] shelves = new List[3];
    private List<String>[][] cardsetMatrix = new List[Level.values().length][Color.values().length];
    private List<String> playerLeadCardDeck = new ArrayList<>();
    private List<Resource> warehouseExtraShelf;
    private List<Resource> secondWarehouseExtraShelf;
    private Resource extraShelfResource;
    private Resource secondExtraShelfResource;
    private Button logOutBtn;
    private Stage dialog = new Stage();

    public Gui(SceneController sceneController){
        this.sceneController = sceneController;
        Platform.setImplicitExit(false);
    }

    public void setSceneController(SceneController sceneController){
        this.sceneController = sceneController;
    }

    @Override
    public void init() {
        this.sceneController.setViewObserver(viewObservers.get(0));
        genericCardSet.loadCards();

        for (int i = 0; i < slots.length; i++) {
            slots[i] = new ArrayList<>();
        }
    }

    @Override
    public void printMessage(String type, String message) {

    }

    @Override
    public void turnMessage(String turnMessage) {

    }

    @Override
    public void notification(String type, String message) {
        notificationPopUp(type, message);
    }

    @Override
    public void askInfo() {
        changeScene("serverInfo");
    }


    @Override
    public void newUsername() {
        ((ServerInfoSceneController) sceneController.getScene("serverInfo")).setErrorLabel("Nickname Aready Taken!");
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void welcome() {
    }

    @Override
    public void showOtherPlayers(int numPlayers, String msg, String[] usernames) {
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            LobbySceneController scene = (LobbySceneController) sceneController.getScene("lobby");
            scene.otherPlayers(usernames);
            scene.setNumPlayers(numPlayers);
        });
        changeScene("lobby");

    }

    @Override
    public void showNewPlayer(String username) {
        Platform.setImplicitExit(false);
        Platform.runLater(() ->{
            LobbySceneController scene = (LobbySceneController) sceneController.getScene("lobby");
            scene.newPlayer(username);
            changeScene("lobby");
        });
    }

    @Override
    public void reconnect() {
        ((ServerInfoSceneController) sceneController.getScene("serverInfo")).reconnect();
    }

    @Override
    public void disconnectedPlayer(String username) {
        notification("info","connection to "+username+" lost, their turn will be skipped until they will reconnect");
    }

    @Override
    public Task<View> getLastTask() {
        return this.lastTask;
    }

    @Override
    public void chooseAction() {
        this.lastTask = new Task<>(View::chooseAction);
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
                    BuyDCSceneController c = (BuyDCSceneController) sceneController.getScene("buyDC");
                    c.setLoadedCards(genericCardSet);
                    PersonalDashboardSceneController c2 = (PersonalDashboardSceneController) sceneController.getScene("personalDashboard");
                    c2.turnOnButtons();
                });
        changeScene("personalDashboard");
    }

    @Override
    public void afterAction() {
        this.lastTask = new Task<>(View::afterAction);

        PersonalDashboardSceneController c = (PersonalDashboardSceneController) sceneController.getScene("personalDashboard");
        c.turnOffButtons();

        Platform.runLater(() -> afterActionPopUp(playerLeadCardDeck));
    }

    @Override
    public void numberOfPlayers() {
        changeScene("numPlayers");
    }

    @Override
    public void playerOrder(String[] players) {
        playerOrderPopUp(players, username);
    }

    @Override
    public void waitingForPlayer(int remaining) {
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            LobbySceneController c = (LobbySceneController) sceneController.getScene("lobby");
            c.setNumPlayers(remaining);
            changeScene("lobby");
        });
    }

    @Override
    public void startGame() {
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            PersonalDashboardSceneController c2 = (PersonalDashboardSceneController) sceneController.getScene("personalDashboard");
            c2.turnOnButtons();
        });
        changeScene("personalDashboard");
    }

    @Override
    public void openMarket() {

    }

    @Override
    public void showMarket() {

    }

    @Override
    public void openCardSet() {


    }

    @Override
    public void handleProduction() {
        lastTask.execute(this);
    }

    @Override
    public void showCardDeck() {

    }

    @Override
    public void chooseResourcesFromMarket(String chosenLine) {

    }

    @Override
    public void purchaseDevelopmentCard(String cardId, int targetDeposit, int slot) {

    }

    @Override
    public void activateProduction(List<String> chosenProductions) {

    }

    @Override
    public void activateProduction(Resource[] chosenProductions) {

    }

    @Override
    public void activateLeaderCard(String cardId) {
    }

    @Override
    public void requestLeaderCard() {
        lastTask = new Task<>(View::requestLeaderCard);
    }

    /**
     * Creates the action of depositing the marble to send to ActionParser.
     * @param marble the marble to store.
     * @param targetDeposit the chosen destination of the marble.
     * @param shelf the chosen warehouse's shelf (0 if targetDeposit is the strongbox).
     */
    private void deposit(String marble, int targetDeposit, int shelf, boolean blackMarble){
        try {
            JSONObject request = requestBuilder
                    .action(Actions.DEPOSIT)
                    .marble(marble)
                    .targetDeposit(targetDeposit)
                    .shelf(shelf)
                    .blackMarble(blackMarble)
                    .buildRequest();
            notify(new Task<>(viewObserver -> viewObserver.handleAction(request)));
        }catch (IllegalArgumentException e){
            lastTask.execute(this);
        }

    }

    @Override
    public void depositMarble(String marble) {
        this.lastTask = new Task<>(view -> new Thread(() -> view.depositMarble(marble)).start());
        notify(new Task<>(ViewObserver::blockIncomingTasks));
        if(marble.equals("SB")){
            blankPopUp();
        }else if(marble.equals("SR")){
            depositFaithPoint();
        }else{
            depositResource(marble);
        }

    }

    @Override
    public void depositMarble(String marble, int targetDeposit) {
        this.lastTask = new Task<>(view -> new Thread(() -> view.depositMarble(marble,targetDeposit)).start());
        notify(new Task<>(ViewObserver::blockIncomingTasks));

        if(marble.equals("SB")){
            blankPopUp();
        }else if(marble.equals("SR")){
            depositFaithPoint();
        }else{
            depositResource(marble, targetDeposit);
        }
    }

    private void depositFaithPoint(){
        Stage dialog = new Stage();
        VBox dialogVbox = new VBox(20);

        dialogVbox.getChildren().add(new Text("You have received a Faith Point!"));
        dialogVbox.getChildren().add(new Text("This will increase your position in the Pope's favor track by 1"));

        Scene dialogScene = new Scene(dialogVbox, 500, 100);
        dialog.setScene(dialogScene);
        dialog.show();

        deposit("SR",1,1,false);
    }

    private void depositBlackMarble(){
        blankPopUp();
    }

    private void depositResource(String marble){
        DepositResourceSceneController c = (DepositResourceSceneController) sceneController.getScene("depositResource");
        c.setResource(marble,extraShelfResource,secondExtraShelfResource);
        changeScene("depositResource");
        //resourcesPopUp(marble);
    }

    private void depositResource(String marble, int targetDeposit){
        DepositResourceSceneController c = (DepositResourceSceneController) sceneController.getScene("depositResource");
        c.setResource(marble,extraShelfResource,secondExtraShelfResource);
        changeScene("depositResource");
        //resourcesPopUp(marble, targetDeposit);
    }

    @Override
    public void updatePersonalDashboard(JSONObject newPDStatus) {
        // Prendiamo le informazioni del JSONObject riguardante la plancia
        JSONObject PFT = (JSONObject) newPDStatus.get("Pope's Favor Track status");
        JSONObject warehouse = (JSONObject) newPDStatus.get("player's warehouse");
        JSONArray warehouseShelves = (JSONArray) warehouse.get("Warehouse's shelves");
        JSONArray devCards = (JSONArray) newPDStatus.get("player's Development Cards");
        JSONArray strongbox = (JSONArray) newPDStatus.get("player's strongbox");

        JSONArray leadCards = (JSONArray) newPDStatus.get("player's Leader Cards");
        leadCards.forEach(lc -> playerLeadCardDeck.add((String) lc));

        JSONObject extraShelvesJson = (JSONObject) newPDStatus.get("extra shelves");
        PersonalDashboardSceneController c = (PersonalDashboardSceneController) sceneController.getScene("personalDashboard");
        c.updatePersonalDashboard(PFT, warehouse, warehouseShelves, devCards, strongbox, extraShelvesJson, slots);
        loadScene("personalDashboard");
    }

    @Override
    public void handleNewLeaderCard(List<String> cards) {
        InitLeaderCardsSceneController c = (InitLeaderCardsSceneController) sceneController.getScene("initialLeadCards");
        c.setLoadedCards(cards);
        changeScene("initialLeadCards");
        notify(new Task<>(cc -> cc.blockIncomingTasks()));
    }

    @Override
    public void updateCardSet(JSONObject cardset) {
        if( cardset.get("chosen Development card") != null) {
            this.genericCardSet.takeCard((String) cardset.get("chosen Development card"));
        }
        JSONArray cards = (JSONArray) cardset.get("remaining Development cards");
        int i =0, j = 0;
        String id;
        for( Level level : Level.values()){
            for( Color color : Color.values() ){
                cardsetMatrix[i][j] = new ArrayList<>();
                for(Object o : cards ){
                    try {
                        id = o.toString();
                        DevelopmentCard card = (DevelopmentCard) genericCardSet.findCard(id);
                        if(card.getLevel() == level.ordinal()+1 && card.getColor() == color){
                            this.cardsetMatrix[i][j].add(card.generateId());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                j++;
            }
            j=0;
            i++;
        }
        ((BuyDCSceneController) sceneController.getScene("buyDC")).updateCardset(cardsetMatrix,genericCardSet);
    }

    @Override
    public void addToSlot(String id, int slot) {
        PersonalDashboardSceneController c = (PersonalDashboardSceneController) sceneController.getScene("personalDashboard");
        c.addToSlot(id, slot);
    }

    @Override
    public void updateMarket(JSONObject market) {
        MarketSceneController c = (MarketSceneController) sceneController.getScene("market");
        c.updateMarket(market);
        loadScene("market");
    }

    @Override
    public void finishMarketHandling() {
        PersonalDashboardSceneController c = (PersonalDashboardSceneController) sceneController.getScene("personalDashboard");
        c.turnOffButtons();
        loadScene("personalDashboard");
        new Thread(() -> notify(new Task<>(ViewObserver::actionFinished))).start();
    }

    @Override
    public void discard(String resource, boolean blackMarble) {

    }

    @Override
    public void updateServerWarehouse(List<Resource>[] warehouse) {
        lastTask.execute(this);
    }

    @Override
    public void notifyExtraStorage(String resource) {
        if(warehouseExtraShelf == null) {
            warehouseExtraShelf = new ArrayList<>();
            extraShelfResource = Resource.valueOf(resource.toUpperCase());
        }else if (secondWarehouseExtraShelf == null){
            secondWarehouseExtraShelf = new ArrayList<>();
            extraShelfResource = Resource.valueOf(resource.toUpperCase());
        }
    }

    @Override
    public void takeTwoDevCards(JSONObject cardIds) {

    }

    @Override
    public void cardActivated(JSONObject cardActivated) {

    }

    @Override
    public void vaticanReportMessage(String vaticanReportMessage, boolean isOnTheRightVaticanZone, int popesCardPoints) {
        if (popesCardPoints == 2) {
            if (isOnTheRightVaticanZone) {
                PersonalDashboardSceneController c = (PersonalDashboardSceneController) sceneController.getScene("personalDashboard");
                c.setPopesFavorTilesBack2();
                loadScene("personalDashboard");
            }
        } else if (popesCardPoints == 3) {
            if (isOnTheRightVaticanZone) {
                PersonalDashboardSceneController c = (PersonalDashboardSceneController) sceneController.getScene("personalDashboard");
                c.setPopesFavorTilesBack3();
                loadScene("personalDashboard");
            }
        } else if (popesCardPoints == 4) {
            if (isOnTheRightVaticanZone) {
                PersonalDashboardSceneController c = (PersonalDashboardSceneController) sceneController.getScene("personalDashboard");
                c.setPopesFavorTilesBack4();
                loadScene("personalDashboard");
            }
        }
    }

    @Override
    public void rankingPositionMessage(String rankingPosition) {
        Platform.runLater(() -> {
            dialog = new Stage();
            VBox dialogVbox = new VBox(20);

            dialogVbox.getChildren().add(new Text("Final raking:"));
            dialogVbox.getChildren().add(new Text(rankingPosition));

            logOutBtn = new Button("LOGOUT");
            dialogVbox.getChildren().add(logOutBtn);

            Scene dialogScene = new Scene(dialogVbox, 500, 300);
            dialog.setScene(dialogScene);
            dialog.show();
            logOutBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onLogOutBtnClick);
        });
    }

    public void onLogOutBtnClick (Event event) {
        dialog.close();
        new Thread(() -> notify(new Task<>(ViewObserver::logOut))).start();
        System.exit(0);
    }

    @Override
    public void lorenzoTurnActionNotification(String actionType, String message) {
        ((PersonalDashboardSceneController) sceneController.getScene("personalDashboard")).lorenzoAction(actionType);
        notificationPopUp("info",message);
    }

    @Override
    public void updateLorenzoPFT(int increment) {
        ((PersonalDashboardSceneController) sceneController.getScene("personalDashboard")).updateLorenzoPFT(increment);
    }

    private void changeScene(String sceneId) {
        Platform.setImplicitExit(false);
        Platform.setImplicitExit(false);
        Platform.runLater(() -> sceneController.changeScene(sceneId));
    }

    private void loadScene(String sceneId) {
        Platform.setImplicitExit(false);
        Platform.runLater(() -> sceneController.loadScene(sceneId));
    }

    private void blankPopUp() {
        //Platform.setImplicitExit(false);
        Platform.runLater(() -> sceneController.blankMarblePopUp());
    }

    private void playerOrderPopUp(String[] players, String username) {
        //Platform.setImplicitExit(false);
        if(players.length != 1) {
            Platform.runLater(() -> sceneController.playerOrderPopUp(players, username));
        }else{
            notificationPopUp("info","You are playing in Single Player mode!");
        }
    }

    private void notificationPopUp(String type, String message) {
        //Platform.setImplicitExit(false);
        Platform.runLater(() -> sceneController.notificationPopUp(type, message));
    }

    private void resourcesPopUp(String marble) {
        //Platform.setImplicitExit(false);
        Platform.runLater(() -> sceneController.resourcesPopUp(marble));
    }

    private void resourcesPopUp(String marble, int targetDeposit) {
        //Platform.setImplicitExit(false);
        Platform.runLater(() -> sceneController.resourcesPopUp(marble, targetDeposit));
    }

    private void afterActionPopUp(List<String> playerLeadCard) {
        //Platform.setImplicitExit(false);
        Platform.runLater(() -> sceneController.afterActionPopUp(playerLeadCard));
    }

}
