package it.polimi.ingsw.controller.controllers;
import it.polimi.ingsw.controller.actions.Action;
import it.polimi.ingsw.controller.actions.ActionParser;
import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.cards.LeaderCard;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.model.containers.Market;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.PopesFavorTrack;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.SpecialType;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.view.VirtualView;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Class GameController. It manages the execution of a single turn.
 */
public class GameController implements ModelObserver {
    private List<Player> playerList;
    protected Player currentPlayer;
    protected final Market market;
    protected CardSet cards;
    private boolean isFinalTurn = false;
    private Player finalTurnCallingPlayer = null;
    private Map<String, VirtualView> virtualViewMap = new HashMap<>();
    private ActionParser actionParser;
    private boolean isTurnEnded = false;
    private boolean hasStarted = false;
    private boolean hasEnded = false;
    protected Queue<Action> actionQueue = new LinkedList<>();
    protected Map<String, Integer> initLeaderCard = new HashMap<>();
    private List<Integer[]> ranking;

    /**
     * Class constructor. It also makes the constructed object an Observer of its market and card set.
     */
    public GameController() {
        playerList = new ArrayList<>();
        market = new Market();
        cards = new CardSet();
        actionParser = new ActionParser(this.playerList, this.market, this.cards);
        // turnController = new TurnController(this.playerList);
        // Si aggiunge la virtual view del nuovo giocatore come Observer del model (PersonalDashboard, CardSet, Market)
        market.addModelObserver(this);
        cards.addModelObserver(this);
    }

    /**
     * Update() method invoked by notify() in Observer pattern.
     * Its only purpose in this case is to accept the Consumer passed by the model according to the "operation" field in the JSONObject parameter of notify().
     * @param operation the Consumer to accept based on the "operation" field of the JSONObject created by model.
     */
    @Override
    public void update(Task<ModelObserver> operation) {
        operation.execute(this);
    }

    /**
     * This method is responsible for updating the market data structure in the Cli of each player.
     * It also allows the updated market to be printed in the Cli of the current player.
     * @param market the JSONObject representing the market updated by the model.
     * @param isInitialized a flag distinguishing two situations: whether the market is being initialized for the first time or it is being updated and hence its status has to be shown to the current player.
     */
    @Override
    public void updateMarket(JSONObject market, boolean isInitialized) {
        broadcast(vv -> vv.updateMarket(market));
        if (isInitialized) {
            if (versionWithFunctioningSockets()){
                getVirtualView(currentPlayer).finishMarketHandling();
            }
        }
    }

    /**
     * This method is responsible for updating the card set (not the player's card deck) data structure in the Cli of each player.
     * @param cardSet the JSONObject representing the card set updated by the model.
     */
    @Override
    public void updateCardSet(JSONObject cardSet) {
        String chosenCard = (String) cardSet.get("chosen Development card");
        broadcast(vv -> vv.updateCardSet(cardSet));
        if (chosenCard != null) {
            broadcast(vv -> vv.actionTypeMessage(chosenCard + " has been purchased"), currentPlayer.getUsername());
            if (versionWithFunctioningSockets()){
                getVirtualView(currentPlayer).addToSlot(chosenCard, (int) cardSet.get("chosen slot"));
                afterAction();
            }
        }
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer() { this.currentPlayer = currentPlayer; }

    public ActionParser getActionParser() {
        return actionParser;
    }

    /**
     * This method broadcasts to all virtual views (listed in VirtualViewMap) an operation they have to execute.
     * @param virtualViewConsumer the operation all virtual views must execute.
     */
    public void broadcast(Consumer<VirtualView> virtualViewConsumer) {
        if (virtualViewMap.values().stream().allMatch(vv -> vv.getPlayerSocket() != null)){
            virtualViewMap.values().forEach(virtualViewConsumer);
        }
    }

    /**
     * This method behaves like broadcast(), but with the difference that a specific virtual view is excluded by the broadcast of an operation.
     * @param virtualViewConsumer the operation the selected group of virtual views must execute.
     * @param excludedUsername    the username associated to the player's virtual view which must not execute virtualViewConsumer.
     */
    public void broadcast(Consumer<VirtualView> virtualViewConsumer, String excludedUsername) {
        if (versionWithFunctioningSockets()){
            virtualViewMap.forEach((usr, vv) -> {
                if (!usr.equals(excludedUsername)) {
                    virtualViewConsumer.accept(vv);
                }
            });
        }
    }



    /**
     * This method allows access to the virtual view of a selected player.
     * @param p the selected player.
     * @return p's virtual view.
     */
    public VirtualView getVirtualView(Player p) {
        return virtualViewMap.get(p.getUsername());
    }

    /**
     * It's the method which initializes all global data structures (market and card set) and assigns each player his/her/their initial assets according to the pre-determined order of players.
     */
    public void start() {
        try {
            hasStarted = true;
            cards.loadCards();
            market.init();
            //picking the first player
            setPlayerOrder();
            //assigning each player four leader cards
            List<LeaderCard> leaderCards = cards.getLeadCardList();
            int rndIndex;
            LeaderCard tmp;
            //shuffling the cards
            for (int i = leaderCards.size() - 1; i > 0; i--) {
                rndIndex = new Random().nextInt(i);
                tmp = leaderCards.get(rndIndex);
                leaderCards.set(rndIndex, leaderCards.get(i));
                leaderCards.set(i, tmp);
            }
            //assigning each player their initial assets, including the leader cards
            for (int i = 0; i < playerList.size(); i++) {
                if (i == 1) {
                    assignMarble(currentPlayer, new Marble(SpecialType.BLACK));
                }
                if (i == 2) {
                    assignMarble(currentPlayer, new Marble(SpecialType.BLACK));
                    currentPlayer.increaseTrackPosition(1);
                }
                if (i == 3) {
                    assignMarble(currentPlayer, new Marble(SpecialType.BLACK));
                    assignMarble(currentPlayer, new Marble(SpecialType.BLACK));
                    currentPlayer.increaseTrackPosition(1);
                }
                for (int j = 0; j < 4; j++) {
                    currentPlayer.getPersonalDashboard().addCard(leaderCards.get(0));
                    getVirtualView(currentPlayer).newLeaderCard(leaderCards.get(0).generateId());
                    leaderCards.remove(0);
                }
                nextPlayer();
            }
            playerList.forEach(player -> {

            });

        } catch (Exception ignored) {
            // e.printStackTrace();
        }
    }

    /**
     * Private method to start handling a marble assigned to a player as an initial asset.
     * @param targetPlayer
     * @param m the marble to process.
     */
    private void assignMarble(Player targetPlayer, Marble m) {
        currentPlayer.getPersonalDashboard().addToTray(m);
        handleTray(targetPlayer, m.generateId());
    }

    /**
     * This method, when invoked, sets off the begin of a single turn.
     */
    public void turnManagement() {
        if (!(isFinalTurn && currentPlayer == finalTurnCallingPlayer)) {
            try {
                isTurnEnded = false;
                if (versionWithFunctioningSockets()){
                    broadcast(vv -> vv.turnMessage(currentPlayer.getUsername()));
                    virtualViewMap.get(currentPlayer.getUsername()).chooseAction();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            broadcast(vv -> vv.sendPriorityMessage("\nHere's the final ranking:\n"));
            printRanking();
        }
    }

    /**
     * This method, when invoked, sets off the end of a single turn and the change of the current player.
     */
    public void changeOfTurn() {
        nextPlayer();
        if (!currentPlayer.isActive())
            nextPlayer();
        turnManagement();
    }

    /**
     * Sets off the final turn.
     */
    public void endGame() {
        if (finalTurnCallingPlayer == null) {
            isFinalTurn = true;
            finalTurnCallingPlayer = currentPlayer;
            if (versionWithFunctioningSockets()){
                String finalTurnMessage = "";
                String currentPlayerFinalTurnMessage = "";
                if (currentPlayer.getPersonalDashboard().getPopesFavorTrack().getTrackPosition() == 24) {
                    finalTurnMessage = currentPlayer.getUsername() + " has reached position #24 in the Faith Track!";
                    currentPlayerFinalTurnMessage = "You have reached position #24 in the Faith Track!";
                } else if (currentPlayer.getPersonalDashboard().getDevCardsDeck().size() == 7) {
                    finalTurnMessage = currentPlayer.getUsername() + " has purchased 7 development cards!";
                    currentPlayerFinalTurnMessage = "You have purchased 7 development cards!";
                }
                String finalTurnMessage1 = finalTurnMessage;
                broadcast(vv -> vv.sendPriorityMessage("\n" + finalTurnMessage1 + " The final turn begins!"), currentPlayer.getUsername());
                String currentPlayerFinalTurnMessage1 = currentPlayerFinalTurnMessage;
                getVirtualView(currentPlayer).sendPriorityMessage("\n" + currentPlayerFinalTurnMessage1 + " You won't make any further actions and wait for the end of the game!");
            }
        }
    }

    /**
     * Sets the order of the player by picking the first randomly
     */
    public void setPlayerOrder() {
        int firstPlayer = (int) (Math.random() * playerList.size());
        currentPlayer = playerList.get(firstPlayer);
        List<String> players = new ArrayList<>();

        for (int i = 0; i <= playerList.size() - 1; i++) {
            playerList.get(firstPlayer).setPosition(i);
            players.add(playerList.get(firstPlayer).getUsername());
            if (firstPlayer == playerList.size() - 1) {
                firstPlayer = 0;
            } else {
                firstPlayer++;
            }
        }
        broadcast(virtualView -> virtualView.playerOrder(players));
    }

    /**
     * Finds the next player in the list and assigns it to the current player position.
     * @return the next current player
     */
    public Player nextPlayer() {
        int activePosition = currentPlayer.getPosition();

        if (activePosition < playerList.size() - 1) {
            currentPlayer = playerList.stream().filter(player -> player.getPosition() == activePosition + 1).findFirst().get();
            currentPlayer.setPosition(activePosition + 1);
        } else {
            currentPlayer = playerList.stream().filter(player -> player.getPosition() == 0).findFirst().get();
            currentPlayer.setPosition(0);
        }

        return currentPlayer;
    }

    /**
     * This method adds a recently connected player to the list of players memorized in this class.
     * @param username the username of the connected player.
     * @param view     the virtual view to associate to him/her/them.
     */
    public void addPlayer(String username, VirtualView view) {
        for (Player player : playerList) {
            if (player.generateId().equals(username)) {
                throw new IllegalArgumentException("username already taken");
            }
        }
        playerList.add(new Player(username));
        virtualViewMap.put(username, view);
        initLeaderCard.put(username, 0);
        getPlayer(username).getPersonalDashboard().addModelObserver(this);
        getPlayer(username).getPersonalDashboard().getWarehouse().addModelObserver(this);
    }

    /**
     * This method returns the player in playerList associated to the username given as parameter of this method.
     * @param username the username of the sought player.
     * @return the player with the username specified in the parameter.
     */
    public Player getPlayer(String username) {
        Optional<Player> p = playerList.stream().filter(u -> u.getUsername().equals(username)).findFirst();
        if (p.isPresent()) {
            return p.get();
        } else {
            throw new IllegalArgumentException("player not found");
        }
    }

    /**
     * This method returns the player in playerList associated to the turn position given as parameter of this method.
     * @param position the position of the sought player in the turn cycle.
     * @return the player with the turn position specified in the parameter.
     */
    public String getPlayer(int position) {
        Optional<Player> p = playerList.stream().filter(u -> u.getPosition() == position).findFirst();
        if (p.isPresent()) {
            return p.get().getUsername();
        } else {
            throw new IllegalArgumentException("player not found");
        }
    }

    /**
     * This method is responsible for carrying out the action built by ActionParser according to the choice of action made in Cli.
     * @param action the action created by ActionParser
     */
    public void handleAction(Action action) {
        actionQueue.add(action);
        emptyActionQueue();
    }

    private synchronized void emptyActionQueue() {
        boolean majorActionexecuted = false;
        Action currentAction;
        while (!actionQueue.isEmpty()) {
            try {
                currentAction = actionQueue.remove();
                if (currentAction.multipleExecutions() || !majorActionexecuted) {
                    if (currentAction.getActionType() != Actions.DEPOSIT && getVirtualView(currentAction.getPlayer()).getPlayerSocket() != null) {
                        Action finalCurrentAction = currentAction;
                        broadcast(vv -> vv.actionMessage(finalCurrentAction.getPlayer().getUsername(), finalCurrentAction.getActionType()), currentAction.getPlayer().getUsername());
                    }
                    try {
                        currentAction.execute();
                        if (getVirtualView(currentAction.getPlayer()).getPlayerSocket() != null){
                            getVirtualView(currentAction.getPlayer()).acceptedAction();
                        }
                        if (!currentAction.multipleExecutions()) {
                            majorActionexecuted = true;
                        }
                    } catch (ActionException e){
                        // e.printStackTrace();
                        if (versionWithFunctioningSockets()){
                            getVirtualView(currentAction.getPlayer()).rejectedAction(e.getMessage());
                        } else {
                            throw e;
                        }
                    }

                } else {
                    getVirtualView(currentAction.getPlayer()).actionTypeMessage("you cannot select this type of action for this turn. you can either activate a leader, or end the turn");
                }
            } catch (ActionException e) {
                if (versionWithFunctioningSockets()){
                    getVirtualView(currentPlayer).rejectedAction(e.getMessage());
                } else {
                    throw e;
                }
            }
        }
    }

    public void endTurn() {
        isTurnEnded = true;
    }

    /**
     * This method sets off the sequence of methods to ask a player (hence their Cli) whether they want to activate one of the Leader Cards they possess.
     */
    public void requestLeaderCard() {
        getVirtualView(currentPlayer).requestLeaderCard();
    }

    /**
     * Method to manage a Vatican Report when a player steps on a Pope's box.
     * @param popesBox the Pope's box the player steps on.
     */
    @Override
    public void vaticanReport(int popesBox, String player) throws IllegalArgumentException {
        Player callingPlayer = getPlayer(player);
        //controllo se questo rapporto in vaticano è già stato chiamato
        if (callingPlayer.getHasBeenCalled()[(popesBox / 8) - 1]) {
            throw new IllegalArgumentException("This Vatican Report has already been called!");
        }
        if (versionWithFunctioningSockets()){
            getVirtualView(callingPlayer).vaticanReportMessage("You have reached Vatican Zone #" + (popesBox / 8) + " and are calling a Vatican Report.", false, true, (popesBox/8)+1);
            getVirtualView(callingPlayer).vaticanReportMessage("You are therefore entitled to " + ((popesBox / 8) + 1) + " points!\n", false, true, (popesBox/8)+1);
        }
        //controllo se il giocatore ha raggiunto questa zona vaticana
        PopesFavorTrack currentPlayerPFT = callingPlayer.getPersonalDashboard().getPopesFavorTrack();
        currentPlayerPFT.hasReachedPopesZone(popesBox);
        //aggiorno le tessere papali accumulate
        int oldPoints = currentPlayerPFT.getPopesCardPoints();
        currentPlayerPFT.setPopesCardPoints(oldPoints + currentPlayerPFT.obtainPopesCardPoints(popesBox));
        //aggiorno i punti degli altri giocatori nella partita multigiocatore
        for (Player p : playerList) {
            //imposto come attivato questo rapporto in vaticano per ogni giocatore
            p.vaticanReportHasBeenCalled(popesBox);
            if (!p.equals(callingPlayer)) {
                PopesFavorTrack otherPlayerPFT = p.getPersonalDashboard().getPopesFavorTrack();
                int playerPosition = otherPlayerPFT.getTrackPosition();
                otherPlayerPFT.hasReachedPopesZone(playerPosition);
                if (versionWithFunctioningSockets())
                    getVirtualView(p).vaticanReportMessage("\n" + callingPlayer.getUsername() + " has called Vatican Report on Vatican Zone #" + (popesBox / 8), true, p.getPersonalDashboard().getPopesFavorTrack().getVaticanZone()[(popesBox/8)-1], (popesBox/8)+1);
                // se un giocatore è in questa zona vaticana, ottieni i punteggi corrispondenti
                if (otherPlayerPFT.getVaticanZone()[(popesBox / 8) - 1]) {
                    otherPlayerPFT.setPopesCardPoints(otherPlayerPFT.getPopesCardPoints() + otherPlayerPFT.obtainPopesCardPoints(popesBox));
                    if (versionWithFunctioningSockets())
                        getVirtualView(p).vaticanReportMessage("\nBecause you are in the same Vatican Zone of the called Vatican Report, you are entitled to " + ((popesBox / 8) + 1) + " additional victory points!", true, p.getPersonalDashboard().getPopesFavorTrack().getVaticanZone()[(popesBox/8)-1], (popesBox/8)+1);
                } else {
                    if (versionWithFunctioningSockets())
                        getVirtualView(p).vaticanReportMessage("\nBecause you are NOT in the same Vatican Zone of the called Vatican Report, you won't be entitled to any additional victory points! :(", true, p.getPersonalDashboard().getPopesFavorTrack().getVaticanZone()[(popesBox/8)-1], (popesBox/8)+1);
                }
            }
            if (versionWithFunctioningSockets())
                p.getPersonalDashboard().notify(p.getPersonalDashboard().notifyEndUpdate());
        }
    }

    @Override
    public void activatedLeader() {
        if (versionWithFunctioningSockets())
            getVirtualView(currentPlayer).notification("Leader activated! ", true);
    }

    public void discardLeaderCard(String player) {
        boolean limitReached = true;

        initLeaderCard.put(player, initLeaderCard.get(player) + 1);

        for (int value : initLeaderCard.values()) {
            if (value != 2) {
                limitReached = false;
            }
        }

        if (limitReached) {
            turnManagement();
        }
    }

    @Override
    public void notifyExtraStorage(String resource) {
        if (versionWithFunctioningSockets())
            getVirtualView(currentPlayer).notifyStorage(resource);
    }

    @Override
    public void takeTwoCards(JSONObject cardIds) {
        broadcast(vv -> vv.takeTwoCards(cardIds));
    }

    @Override
    public void cardActivated(JSONObject cardActivated) {
        if (versionWithFunctioningSockets()){
            getVirtualView(currentPlayer).cardActivated(cardActivated);
        }
    }


    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public Market getMarket() {
        return market;
    }

    /**
     * This method sets off the sequence of methods to deposit a resource in the depot.
     * The tray contains a marble to process and this marble is associated to a determined resource.
     *
     * @param targetPlayer the player whose tray is being processed.
     * @param marble       the marble in the tray to process.
     */
    @Override
    public void handleTray(String targetPlayer, String marble) {
        if (versionWithFunctioningSockets()) {
            handleTray(getPlayer(targetPlayer), marble);
        }
    }

    private void handleTray(Player targetPlayer, String marble) {
        getVirtualView(targetPlayer).handleTray(marble);
    }

    @Override
    public void updatePersonalDashboard(JSONObject newPDStatus) {
        if (versionWithFunctioningSockets()){
            getVirtualView(getPlayer((String) newPDStatus.get("player"))).updatePersonalDashboard(newPDStatus);
        }
    }

    @Override
    public void updatePFT(int newPosition, String player) {
        broadcast(virtualView -> virtualView.updatePFT(newPosition, player));
    }

    /**
     * This method is called when an action has just been executed.
     * This allows the player to access a new set of actions to perform before finishing their turn.
     */
    @Override
    public void afterAction() {
        if (versionWithFunctioningSockets()){
            getVirtualView(currentPlayer).afterAction();
        }
    }

    public void setPlayerActive(String player, VirtualView newView) throws IllegalArgumentException {
        Player p = getPlayer(player);
        //reinserisce il giocatore nel ciclo dei turni
        p.isActive(true);
        virtualViewMap.put(player,newView);
        //aggiorna il giocatore
        VirtualView view = getVirtualView(p);
        view.updateMarket(market.createJSONObject(true));
        view.updateCardSet(cards.createJSONObject(null));
        view.updatePFT(p.getPersonalDashboard().getPopesFavorTrack().getTrackPosition(),p.getUsername());
        view.updatePersonalDashboard(p.getPersonalDashboard().createJSONObject());
    }

    public void setPlayerInactive(String player) throws IllegalArgumentException {
        getPlayer(player).isActive(false);
        if (player.equals(currentPlayer.getUsername())) {
            changeOfTurn();
        }
    }

    protected void addVirtualView(Player p, VirtualView virtualView) {
        this.virtualViewMap.put(p.getUsername(), virtualView);
    }

    protected void addToPlayerList(Player p) {
        this.playerList.add(p);
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public boolean hasEnded() {
        return hasEnded;
    }

    /**
     * This method elaborates the final ranking of the game.
     */
    @SuppressWarnings("unchecked")
    public void printRanking() {
        ranking = new ArrayList<>();
        for (Player p : playerList) {
            // Assegniamo a ciascun giocatore una tripla: posizione nel giro turni, punti vittoria e n° risorse nello stock.
            Integer[] position = new Integer[3];
            position[0] = p.getPosition();
            position[1] = p.getAllVictoryPoints();
            position[2] = p.getResourcesInStock();
            ranking.add(position);
        }
        // Prima di tutto ordiniamo la lista in ordine decrescente in base ai punti vittoria (posizione 1 nell'array).
        ranking.sort((o1, o2) -> o2[1].compareTo(o1[1]));
        List<Integer> victoryPoints = new ArrayList<>();
        ranking.forEach(pos -> victoryPoints.add(pos[1]));
        // Se tra i punti vittoria dei giocatori c'è almeno un duplicato, c'è un pareggio: bisogna guardare il numero di risorse nello stock.
        if (hasDuplicate(victoryPoints)) {
            List<Integer> equalPoints = getDuplicate(victoryPoints);
            // In caso di pareggio, rimuoviamo dalla lista della classifica la sottolista con i giocatori con punteggio uguale, la ordiniamo in base al nuovo criterio e poi la reinseriamo in classifica.
            // Il for serve per prevenire situazioni in cui c'è un doppio pareggio.
            for (Integer point : equalPoints){
                List<Integer[]> subRanking = ranking.stream().filter(pos -> pos[1].equals(point)).collect(Collectors.toList());
                ranking.removeAll(subRanking);
                subRanking.sort((o1, o2) -> o2[2].compareTo(o1[2]));
                ranking.addAll(subRanking);
            }
            // Una volta reinserita in classifica la sottolista, bisogna assicurarsi che la lista della classifica sia ordinata.
            ranking.sort((o1, o2) -> o2[1].compareTo(o1[1]));
        }
        AtomicInteger i = new AtomicInteger(1);
        broadcast(vv -> {
            ranking.forEach(pos -> vv.rankingPositionMessage(i + ") " + getPlayer(pos[0])));
            i.getAndIncrement();
        });
        broadcast(VirtualView::invokeLogOut);
    }

    public <T> boolean hasDuplicate(Iterable<T> all) {
        Set<T> set = new HashSet<>();
        // Set#add returns false if the set does not change, which
        // indicates that a duplicate element has been added.
        for (T each: all) if (!set.add(each)) return true;
        return false;
    }

    public static <T> List getDuplicate(Collection<T> list) {

        final List<T> duplicatedObjects = new ArrayList<T>();
        Set<T> set = new HashSet<T>() {
            @Override
            public boolean add(T e) {
                if (contains(e)) {
                    duplicatedObjects.add(e);
                }
                return super.add(e);
            }
        };
        for (T t : list) {
            set.add(t);
        }
        return duplicatedObjects;
    }

    protected boolean versionWithFunctioningSockets() {
        return virtualViewMap.values().stream().allMatch(vv -> vv.getPlayerSocket() != null);
    }

    public CardSet getCards() {
        return cards;
    }

    public boolean isFinalTurn() { return isFinalTurn; }

    public Player getFinalTurnCallingPlayer() {
        return finalTurnCallingPlayer;
    }

    public List<Integer[]> getRanking() {
        return ranking;
    }

    @Override
    public void updateLorenzoPFT(int increment){
        broadcast(virtualView -> virtualView.updateLorenzoPFT(increment));
    }
}