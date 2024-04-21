package it.polimi.ingsw.view;

import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.task.Task;
import org.json.simple.JSONObject;

import java.util.List;

public interface View {

    /**
     * Initializes Cli by adding all cards to the card set.
     */
    void init();

    /**
     * This method prints a message of a specified type.
     * @param type the type of message (e.g. "error").
     * @param message the message to print.
     */
    void printMessage(String type, String message);

    /**
     * This method prints the message indicating the begin of a player's turn.
     * @param turnMessage a message indicating "It's your turn!" if it's the player's turn, otherwise "It's [other player nickname]'s turn!".
     */
    void turnMessage(String turnMessage);

    void notification(String type, String message);


    /**
     * The player is asked to give their credentials.
     */
    void askInfo();

    /**
     * This method is triggered if a player tries to take a nickname which was already chosen.
     * No duplicate nicknames are admitted in any game.
     */
    void newUsername();


    void setUsername(String username);

    void welcome();

    void showOtherPlayers(int numPlayers, String msg,String[] usernames);

    void showNewPlayer(String username);

    /**
     * Notifies the player that they have just disconnected and asks whether they want to join the game again.
     */
    void reconnect();

    /**
     * This method prints out that a player has disconnected from the game.
     * @param username the nickname of the disconnected player.
     */
    void disconnectedPlayer(String username);

    Task<View> getLastTask();

    /**
     * This method shows the player all actions that can be executed only once.
     */
    void chooseAction();

    /**
     * This method shows the player all actions that can be executed more times than once.
     */
    void afterAction();


    /**
     * The first connected player is asked how many players will join this game.
     */
    void numberOfPlayers();

    /**
     * This method shows the order in which players will play.
     * @param players the players of the game.
     */
    void playerOrder(String[] players);

    /**
     * This method prints how many other players are expected to join before the game begins.
     * @param remaining the remaining number of players expected to connect.
     */
    void waitingForPlayer(int remaining);

    /**
     * This method sets off the begin of the game.
     */
    void startGame();


    // Metodi per aprire i containers
    /**
     * Shows the current status of the market and asks the current player which line of marbles they want to purchase from the market.
     */
    void openMarket();

    /**
     * Shows the market data structure itself at the point this method is invoked.
     */
    void showMarket();

    /**
     * Shows all available development cards in card set, the current player decides which one to take based on the ID and potentially purchases it if they have enough resources in the strongbox or the warehouse's shelves.
     */
    void openCardSet();

    /**
     * Handles the production a player wants to activate.
     */
    void handleProduction();

    void showCardDeck();


    // I metodi principali
    void chooseResourcesFromMarket(String chosenLine);

    /**
     * Requests the action to purchase a development card and take resources from the warehouse or the strongbox.
     * @param cardId the ID of the chosen development card.
     * @param targetDeposit 1 = strongbox, 2 = warehouse.
     */
    void purchaseDevelopmentCard(String cardId, int targetDeposit, int slot);

    /**
     * Activates the production of one or more development cards.
     * @param chosenProductions the IDs of the development cards whose production is being activated.
     */
    void activateProduction(List<String> chosenProductions);

    /**
     * Activates a basic production.
     * @param chosenProductions the array containing the input (first two elements) and the output (last element) of a basic production.
     */
    void activateProduction(Resource[] chosenProductions);

    /**
     * Activates one of the two leader cards the current player possesses (if they have the necessary requirements).
     * @param cardId the ID of the chosen leader card.
     */
    void activateLeaderCard(String cardId);

    /**
     * Asks the current player whether they want to activate a leader card.
     */
    void requestLeaderCard();

    /**
     * This method is the second method called while processing a marble in the tray.
     * It discriminates the situation based on the type of the processed marble.
     * @param marble the marble to process (RB = shield, RG = stone, RP = servant, RY = coin, SR = faith point).
     */
    void depositMarble(String marble);

    /**
     * This method is the first method called while processing a marble in the tray.
     * It discriminates the situation based on where the marble has to be deposited.
     * @param marble the marble to process.
     * @param targetDeposit the stock where to deposit the marble (1 = strongbox, 2 = warehouse).
     */
    void depositMarble(String marble, int targetDeposit);


    /**
     * This method notifies the view of the updated status of the player's personal dashboard.
     * @param newPDStatus the JSONObject with the information about the information about the player's personal dashboard.
     */
    void updatePersonalDashboard(JSONObject newPDStatus);

    /**
     * Handles the leader card selection at the beginning of the game
     * @param cards contains the ID of the leader cards assigned to the player
     */
    void handleNewLeaderCard(List<String> cards);


    /**
     * Updates the status of card set data structure based on the status received by the server.
     * @param cardSet the JSONObject summing up the new card set status.
     */
    void updateCardSet(JSONObject cardSet);

    /**
     * Adds a card to a specific slot
     * @param id the id of the card
     * @param slot to slot to insert the card in
     */
    void addToSlot(String id, int slot);

    /**
     * Updates the status of market data structure based on the status received by the server.
     * @param market the JSONObject summing up the new market status.
     */
    void updateMarket(JSONObject market);

    /**
     * Shows the new market status to the player.
     */
    void finishMarketHandling();

    /**
     * This method handles the discard of a resource from the tray.
     * @param resource the resource to discard.
     * @param blackMarble a flag indicating whether a marble is black (hence a free-choice resource is being discarded).
     */
    void discard(String resource, boolean blackMarble);

    /**
     * This method is called when a player moves their resources in the warehouse, hence the data structure in model has to be consequently updated.
     * @param warehouse the warehouse status to send to the server.
     */
    void updateServerWarehouse(List<Resource>[] warehouse);

    /**
     * This method is called to create the extra shelf in the view as a consequence of the server update.
     * @param resource the resource the extra shelf is reserved to.
     */
    void notifyExtraStorage(String resource);

    /**
     * This method updates the generic view's cardset after Lorenzo takes two development cards.
     * @param cardIds the IDs of the taken development cards.
     */
    void takeTwoDevCards(JSONObject cardIds);

    /**
     * This method notifies the view that a development card has been used, hence its production can't be activated anymore.
     * @param cardActivated the JSONObject with the information about the card whose production has just been activated.
     */
    void cardActivated(JSONObject cardActivated);

    /**
     * This method forwards Vatican report messages to the client's view.
     * @param vaticanReportMessage the message to forward.
     * @param isOnTheRightVaticanZone whether the player is in the right vatican zone during the report.
     * @param popesCardPoints the number of victory points obtained by the Pope's card of that vatican zone.
     */
    void vaticanReportMessage(String vaticanReportMessage, boolean isOnTheRightVaticanZone, int popesCardPoints);

    /**
     * This method forwards final ranking messages to the client's view.
     * @param rankingPosition the line with the position, the player in that position in the ranking and the number of victory points they gained.
     */
    void rankingPositionMessage(String rankingPosition);

    /**
     * This method forwards messages about Lorenzo making an action during his turn.
     * @param actionType the type of action (Actions.LORENZO or Actions.DEVCARDWITHDRAW) he is performing.
     * @param message the message attached to the kind of action.
     */
    void lorenzoTurnActionNotification(String actionType, String message);

    /**
     * Called to increment lorenzo pft
     * @param increment increment
     */
    void updateLorenzoPFT(int increment);
}
