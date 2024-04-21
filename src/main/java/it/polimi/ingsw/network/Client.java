package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.observer.ViewObserver;
import it.polimi.ingsw.view.View;
import org.json.simple.JSONObject;

import java.util.List;

public interface Client extends NetworkController, ViewObserver {
    /**
     * Notifies the client that the maximum number of players has been reached.
     */
    void maximumPlayersReached();

    /**
     * Notifies the client that the game is starting.
     */
    void gameHasStarted();

    /**
     * Notifies the client that a certain nickname has already been taken.
     */
    void usernameAlreadyTaken();

    View getView();

    /**
     * Sends the client a generic message.
     * @param msg the message.
     */
    void notification(String msg);

    /**
     * Notifies the client that they are trying to reconnect.
     */
    void reconnectPlayer();

    /**
     * Notifies the player that they have disconnected.
     * @param player the disconnected player.
     */
    void playerDisconnected(String player);

    /**
     * Notifies the player that they have reconnected.
     * @param player the reconnected player.
     */
    void playerReconnected(String player);

    /**
     * Shows the lobby to the player.
     * @param players the players in the game.
     * @param remaining how many players are expected to start the game.
     */
    void lobby(String players, int remaining);

    /**
     * Shows the player the order they will carry out the game.
     * @param players the players in the game.
     */
    void playerOrder(String[] players);

    /**
     * Notifies the client that they are starting their turn.
     */
    void startTurn();

    void forwardMessage(String message);

    /**
     * Notifies the client that someone is performing a certain kind of action.
     * @param actions the action performed.
     * @param username the username of the performer.
     */
    void actionMessage(Actions actions, String username);

    /**
     * Notifies the client that an action execution has been denied.
     * @param error the error message.
     */
    void actionRejected(String error);

    /**
     * Notifies the client that an action execution has been successfully accepted.
     */
    void actionAccepted();

    /**
     * Shows the client the beginning of the turn.
     * @param username the current player of a turn.
     */
    void turnMessage(String username);

    /**
     * The client requests a leader card.
     */
    void requestLeaderCard();

    /**
     * Shows the player the 4 beginning possible leader cards they can choose from.
     * @param card the inserted card.
     */
    void newLeaderCard(String card);
    void timeForDisposal(String resource, boolean blackMarble);

    /**
     * The tray of marbles is being handled before depositing in the warehouse.
     * @param marble the processed marble.
     */
    void handleTray(String marble);

    /**
     * Shows the client the new status of the personal dashboard.
     * @param newPDStatus the new personal dashboard status.
     */
    void updatePersonalDashboard(JSONObject newPDStatus);

    /**
     * Shows the client the new position of Lorenzo.
     * @param newPosition the new position of Lorenzo.
     * @param username the single player's username.
     */
    void updatePFT(int newPosition,String username);

    /**
     * Shows the client that Lorenzo has increased his position.
     * @param increment the number of position he went forward.
     */
    void incrementLorenzoPFT(int increment);

    /**
     * This method is called after one-time actions.
     */
    void afterAction();

    /**
     * Notifies the client that extra shelves have been created due to storage leader card bonus.
     * @param resource the resource assigned to the extra shelf.
     */
    void notifyExtraStorage(String resource);

    /**
     * Notifies the player that Lorenzo has taken two development cards.
     * @param cardIds the IDs of the taken cards.
     */
    void takeTwoCards(JSONObject cardIds);

    /**
     * Notifies the client that a card has been activated.
     * @param cardActivated the JSONObject representing the activated card.
     */
    void cardActivated(JSONObject cardActivated);

    /**
     * Notifies the client about a message of a Vatican report.
     * @param vaticanReportMessage the message.
     * @param isOnTheRightVaticanZone if the player can get the Pope's card points.
     * @param popesCardPoints the number of Pope's card points.
     */
    void vaticanReportMessage(String vaticanReportMessage, boolean isOnTheRightVaticanZone, int popesCardPoints);

    /**
     * Shows the player a position in the ranking.
     * @param rankingPosition the string of a position in the ranking.
     */
    void rankingPositionMessage(String rankingPosition);

    /**
     * Notifies the single player that Lorenzo is performing his turn.
     * @param actionType the Lorenzo action type.
     * @param message the message.
     */
    void lorenzoTurnActionNotification(String actionType, String message);
}
