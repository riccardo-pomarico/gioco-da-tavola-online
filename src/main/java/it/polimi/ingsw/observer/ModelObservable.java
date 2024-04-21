package it.polimi.ingsw.observer;

import it.polimi.ingsw.task.Task;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ModelObservable {
    private final List<ModelObserver> modelObservers = new ArrayList<>();
    private JSONObject jsonObject;
    private static final Map<String,Task<ModelObservable>> updateMap = new HashMap<>();

    public void addModelObserver(ModelObserver obs) {
        modelObservers.add(obs);
    }

    static {
        updateMap.put("UPDATE MARKET", new Task<>(ModelObservable::updateMarket));
        updateMap.put("PERSONAL DASHBOARD'S UPDATE", new Task<>(ModelObservable::updatePersonalDashboard));
        updateMap.put("PURCHASE DEVELOPMENT CARDS", new Task<>(ModelObservable::updateCardSet));
        updateMap.put("TRAY", new Task<>(ModelObservable::handleTray));
        updateMap.put("PFT UPDATE", new Task<>(ModelObservable::updatePFT));
        updateMap.put("VATICAN REPORT", new Task<>(ModelObservable::vaticanReport));
        updateMap.put("LEADER ACTIVATION", new Task<>(ModelObservable::activatedLeader));
        updateMap.put("END OF PERSONAL DASHBOARD'S UPDATE", new Task<>(ModelObservable::callForEndOfAction));
        updateMap.put("DISCARD LEADER CARD", new Task<>(ModelObservable::discardLeaderCard));
        updateMap.put("ADD EXTRA STORAGE", new Task<>(ModelObservable::extraStorage));
        updateMap.put("LORENZO TAKES TWO CARDS", new Task<>(ModelObservable::takeTwoCards));
        updateMap.put("LORENZO PFT INCREMENT", new Task<>(ModelObservable::lorenzoUpdate));
        updateMap.put("CARD WAS USED", new Task<>(ModelObservable::cardActivated));
        updateMap.put("FINAL TURN", new Task<>(ModelObservable::finalTurn));
    }

    /**
     * Operation: discarding a leader card.
     * The execution of the task specifying this method triggers the method in game controller which handles the discard of a leader card.
     */
    private void discardLeaderCard() {
        modelObservers.forEach(modelObserver -> modelObserver.discardLeaderCard((String) jsonObject.get("player")));
    }

    /**
     * Operation: updating the market data structure.
     * The execution of the task specifying this method triggers the method in game controller which handles the update of the market.
     */
    private void updateMarket(){
        modelObservers.forEach(modelObserver -> {
            if ((boolean) jsonObject.get("is initialized")) {
                modelObserver.update(new Task<>(gc -> gc.updateMarket(jsonObject, true)));
            } else {
                modelObserver.update(new Task<>(gc -> gc.updateMarket(jsonObject, false)));
            }
        });
    }

    /**
     * Operation: updating the player's personal dashboard.
     * The execution of the task specifying this method triggers the method in game controller which handles the update of the personal dashboard.
     */
    private void updatePersonalDashboard() {
        modelObservers.forEach(modelObserver -> modelObserver.updatePersonalDashboard(jsonObject));
    }

    /**
     * Operation: updating the general card set.
     * The execution of the task specifying this method triggers the method in game controller which handles the update of the card set.
     */
    private void updateCardSet() {
        modelObservers.forEach(modelObserver -> modelObserver.updateCardSet(jsonObject));
    }

    /**
     * Operation: processing a marble present in the tray.
     * The execution of the task specifying this method triggers the method in game controller which handles the tray.
     */
    private void handleTray() {
        modelObservers.forEach(modelObserver -> modelObserver.handleTray(jsonObject.get("player").toString(),jsonObject.get("marble").toString()));
    }

    /**
     * Operation: updating the player's Pope's Favor Track.
     * The execution of the task specifying this method triggers the method in game controller which handles the update of the Pope's Favor Track.
     */
    private void updatePFT() {
        modelObservers.forEach(modelObserver -> modelObserver.updatePFT((int) jsonObject.get("Player's track position"),(String) jsonObject.get("player")));
    }

    /**
     * Operation: notifying the event of a Vatican report.
     * The execution of the task specifying this method triggers the method in game controller which handles the notification of a Vatican report.
     */
    private void vaticanReport(){
        modelObservers.forEach(modelObserver -> modelObserver.vaticanReport((int) jsonObject.get("popesBox"), (String) jsonObject.get("player")));
    }

    /**
     * Operation: activating a leader card.
     * The execution of the task specifying this method triggers the method in game controller which handles the notification that a player has activated a leader card.
     */
    private void activatedLeader(){
        modelObservers.forEach(ModelObserver::activatedLeader);
    }

    /**
     * Operation: notifying that extra storage in warehouse has been created.
     * The execution of the task specifying this method triggers the method in game controller which will notify the client that extra storage is being created.
     */
    private void extraStorage() { modelObservers.forEach(modelObserver -> modelObserver.notifyExtraStorage((String) jsonObject.get("resource"))); }

    /**
     * Operation: notifying the end of a single-time action.
     * The execution of the task specifying this method triggers the method in game controller which will ask the client which multiple-time action they want to execute.
     */
    private void callForEndOfAction() {
        modelObservers.forEach(ModelObserver::afterAction);
    }

    /**
     * Operation: Lorenzo is taking two development cards.
     * The execution of the task specifying this method triggers the method in game controller which handles the discard of two development cards from the general card set because of an action during Lorenzo's turn.
     */
    private void takeTwoCards() {
        modelObservers.forEach(modelObserver -> modelObserver.takeTwoCards(jsonObject));
    }

    /**
     * Operation: the production of a development card has been activated.
     * The execution of the task specifying this method triggers the method in game controller which will notify that the production of a development card has been activated and can't be activate once again.
     */
    private void cardActivated() { modelObservers.forEach(modelObserver -> modelObserver.cardActivated(jsonObject));}

    /**
     * Operation: a player has reached the game termination condition, hence the final turn has begun.
     * The execution of the task specifying this method triggers the method in game controller which will notify all players that the following turn is the final one.
     */
    private void finalTurn() { modelObservers.forEach(ModelObserver::endGame); }

    private void lorenzoUpdate(){
        modelObservers.forEach(modelObserver -> modelObserver.updateLorenzoPFT((int) jsonObject.get("increment")));
    }


    /**
     * Notify method of the Observer pattern between model and game controller.
     * @param jsonObject the JSONObject passed by the model and containing the operation which defines the method called by multiplayer/single player game controller.
     * Based on the operation field of the JSONObject, one of the private methods listed in updateMap is executed.
     */
    public void notify(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        updateMap.get(((String) jsonObject.get("operation")).toUpperCase()).execute(this);
    }
}
