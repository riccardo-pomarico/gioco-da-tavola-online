package it.polimi.ingsw.view;

import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.network.Client;
import it.polimi.ingsw.network.SocketHandler;
import it.polimi.ingsw.task.ClientTask;
import it.polimi.ingsw.task.NetworkTask;
import org.json.simple.JSONObject;

import java.util.*;

public class VirtualView {
    private SocketHandler playerSocket;

    public VirtualView(SocketHandler socketHandler) {
        playerSocket = socketHandler;
    }

    public VirtualView() {}

    public SocketHandler getPlayerSocket() {
        return playerSocket;
    }

    public void turnMessage(String usr) {
        playerSocket.sendTask(new ClientTask(cc -> cc.turnMessage(usr)));
    }

    public void actionTypeMessage(String actionTypeMessage) {
        playerSocket.sendTask(new ClientTask(cc -> cc.forwardMessage(actionTypeMessage)));
    }
    public void sendPriorityMessage(String priorityMessage) {
        ClientTask ct = new ClientTask(cc -> cc.forwardMessage(priorityMessage));
        ct.setPriority();
        playerSocket.sendTask(ct);
    }
    public void notification(String msg, boolean update){
        NetworkTask clientTask = new ClientTask(client -> client.notification(msg));
        if(update){
            clientTask.setPriority();
        }
        playerSocket.sendTask(clientTask);
    }
    public void playerOrder(List<String> players){
        String[] playerArray = new String[players.size()];
        for(int i = 0; i<playerArray.length; i++){
            playerArray[i] = players.get(i);
        }
        playerSocket.sendTask(new ClientTask(cc -> cc.playerOrder(playerArray)));
    }

    public String[] askInfo() {
        return new String[] {"",""};
    }

    public void chooseAction() {
        playerSocket.sendTask(new ClientTask(Client::startTurn));
    }

    public void updateMarket(JSONObject market) {
        playerSocket.sendTask(new ClientTask(client -> client.updateMarket(market)));
    }

    public void newLeaderCard(String card){
        NetworkTask task = new ClientTask(client -> client.newLeaderCard(card));
        task.setPriority();
        playerSocket.sendTask(task);
    }

    public void finishMarketHandling() {
        playerSocket.sendTask(new ClientTask(Client::finishMarketHandling));
    }

    public void updateCardSet(JSONObject cardSet) {
        ClientTask ct = new ClientTask(client -> client.updateCardSet(cardSet));
        ct.setPriority();
        playerSocket.sendTask(ct);
    }

    public void requestLeaderCard() {
        playerSocket.sendTask(new ClientTask(Client::requestLeaderCard));
    }

    public void actionMessage(String username, Actions action){
        playerSocket.sendTask(new ClientTask(client -> client.actionMessage(action,username)));
    }
    public void rejectedAction(String error){
        NetworkTask operation = new ClientTask(client -> client.actionRejected(error));
        operation.setPriority();
        playerSocket.sendTask(operation);
    }
    public void acceptedAction(){
        NetworkTask operation = new ClientTask(Client::actionAccepted);
        operation.setPriority();
        playerSocket.sendTask(operation);
    }

    public void afterAction() {
        NetworkTask task = new ClientTask(Client::afterAction);
        playerSocket.sendTask(task);
    }

    public void handleTray(String marble){
        playerSocket.sendTask(new ClientTask(client -> client.handleTray(marble)));
    }

    public void updatePersonalDashboard(JSONObject newPDStatus) {
        ClientTask ct = new ClientTask(client -> client.updatePersonalDashboard(newPDStatus));
        ct.setPriority();
        playerSocket.sendTask(ct);
    }

    public void updatePFT(int newPosition,String username) {
        ClientTask ct = new ClientTask(client -> client.updatePFT(newPosition,username));
        ct.setPriority();
        playerSocket.sendTask(ct);
    }

    public void addToSlot(String id, int slot) {
        playerSocket.sendTask(new ClientTask(client -> client.addToSlot(id, slot)));
    }

    public void notifyStorage(String resource) {
        ClientTask ct = new ClientTask(client -> client.notifyExtraStorage(resource));
        ct.setPriority();
        playerSocket.sendTask(ct);
    }

    public void takeTwoCards(JSONObject cardIds) {
        playerSocket.sendTask(new ClientTask(client -> client.takeTwoCards(cardIds)));
    }

    public void cardActivated(JSONObject cardActivated) {
        playerSocket.sendTask(new ClientTask(client -> client.cardActivated(cardActivated)));
    }

    public void invokeLogOut() {
        playerSocket.sendTask(new ClientTask(Client::logOut));
    }

    public void vaticanReportMessage(String vaticanReportMessage, boolean priority, boolean isOnTheRightVaticanZone, int popesCardVictoryPoints) {
        ClientTask ct = new ClientTask(client -> client.vaticanReportMessage(vaticanReportMessage, isOnTheRightVaticanZone, popesCardVictoryPoints));
        if (priority)
            ct.setPriority();
        playerSocket.sendTask(ct);
    }

    public void rankingPositionMessage(String rankingPosition) {
        playerSocket.sendTask(new ClientTask(client -> client.rankingPositionMessage(rankingPosition)));
    }

    public void lorenzoTurnActionNotification(String actionType, String message) {
        playerSocket.sendTask(new ClientTask(client -> client.lorenzoTurnActionNotification(actionType, message)));
    }
    public void updateLorenzoPFT(int increment){
        NetworkTask task = new ClientTask(client -> client.incrementLorenzoPFT(increment));
        task.setPriority();
        playerSocket.sendTask(task);
    }
}
