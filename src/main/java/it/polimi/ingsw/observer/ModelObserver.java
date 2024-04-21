package it.polimi.ingsw.observer;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.task.Task;
import org.json.simple.JSONObject;

import java.util.List;

public interface ModelObserver {
    void update(Task<ModelObserver> operation);
    void updateMarket(JSONObject newMarketStatus, boolean isInitialized);
    void updateCardSet(JSONObject cardSet);
    void handleTray(String player, String marble);
    void updatePersonalDashboard(JSONObject newPDStatus);
    void updatePFT(int newPosition,String player);
    void vaticanReport(int popesBox, String player);
    void activatedLeader();
    void afterAction();
    void discardLeaderCard(String player);
    void notifyExtraStorage(String resource);
    void takeTwoCards(JSONObject cardIds);
    void cardActivated(JSONObject cardActivated);
    void updateLorenzoPFT(int increment);
    void endGame();
}
