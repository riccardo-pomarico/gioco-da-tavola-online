package it.polimi.ingsw.observer;

import it.polimi.ingsw.task.Task;
import org.json.simple.JSONObject;

public interface ViewObserver {
    void playerInfo(String[] info);
    void startGame();
    void update(Task<ViewObserver> operation);
    void setNumberOfPlayers(int finalNumPlayers);
    void logIn(String usr);
    void logOut();
    void reconnectToServer();
    void handleAction(JSONObject request);
    void blockIncomingTasks();
    void unblockIncomingTasks();
    void updateMarket(JSONObject market);
    void finishMarketHandling();
    void updateCardSet(JSONObject cardSet);
    void addToSlot(String id, int slot);
    void actionFinished();
    void timeForLeaderCard();
    void changeOfTurn();
    void afterAction();
}
