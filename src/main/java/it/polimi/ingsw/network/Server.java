package it.polimi.ingsw.network;

import org.json.simple.JSONObject;

public interface Server extends NetworkController{
    void setNumPlayers(int maxNumPlayers);
    void logIn(String username);
    void reconnect(String username);
    void requestAction(JSONObject request);
    void actionFinished();
    void timeForLeaderCard();
    void changeOfTurn();
    void startGame();
    void logOut();
}
