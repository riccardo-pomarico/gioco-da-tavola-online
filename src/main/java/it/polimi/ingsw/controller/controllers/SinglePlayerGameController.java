package it.polimi.ingsw.controller.controllers;

import it.polimi.ingsw.controller.actions.Action;
import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.controller.actions.DiscardAction;
import it.polimi.ingsw.controller.actions.LorenzoAction;
import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.cards.LeaderCard;
import it.polimi.ingsw.model.player.PopesFavorTrack;
import it.polimi.ingsw.model.player.SinglePlayer;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.view.VirtualView;

import java.util.List;
import java.util.Random;

public class SinglePlayerGameController extends GameController {
    private SinglePlayer singlePlayer;
    private boolean isEndTurn = false;
    private boolean defeat = false;

    public SinglePlayerGameController() {
        super();
    }

    /**
     * Method adding the single player to the game.
     * @param username the username of the connected player.
     * @param view the virtual view to associate to him/her/them.
     */
    @Override
    public void addPlayer(String username, VirtualView view) throws IllegalStateException {
        if (singlePlayer != null) {
            throw new IllegalStateException("player was already initialized");
        }
        singlePlayer = new SinglePlayer(username, cards);
        addVirtualView(singlePlayer, view);
        addToPlayerList(singlePlayer);
        getPlayer(username).getPersonalDashboard().addModelObserver(this);
        getPlayer(username).getPersonalDashboard().getWarehouse().addModelObserver(this);
        currentPlayer = singlePlayer;
        singlePlayer.getPersonalDashboard().getPopesFavorTrack().setTrackPosition(0);
        initLeaderCard.put(username, 0);
    }

    /**
     * Method setting off the begin of the action of this class.
     */
    @Override
    public void start(){
        market.init();
        cards.loadCards();
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
        for (int j = 0; j < 4; j++) {
            currentPlayer.getPersonalDashboard().addCard(leaderCards.get(0));
            getVirtualView(currentPlayer).newLeaderCard(leaderCards.get(0).generateId());
            leaderCards.remove(0);
        }
    }

    /**
     * Overridden method managing the turn of the single player game.
     */
    public void turnManagement() {
        if (!isEndTurn) {
            if (versionWithFunctioningSockets()) {
                broadcast(vv -> vv.turnMessage(currentPlayer.getUsername()));
                getVirtualView(singlePlayer).chooseAction();
            }
        } else {
            try {
                lorenzoTurn();
            } catch (IllegalAccessException e) {
                turnManagement();
            }
        }
    }

    /**
     * This method handles the passage between the player's and Lorenzo's turn and viceversa.
     */
    public void changeOfTurn() {
        setEndTurn(!isEndTurn);
        turnManagement();
    }

    public SinglePlayer getSinglePlayer() {
        return singlePlayer;
    }

    /**
     * This method handles Vatican Report in case of a single player game.
     * @param popesBox the Pope's box the player or Lorenzo steps on.
     * @throws IllegalArgumentException if called when the player or Lorenzo is not on a Pope's box
     */
    @Override
    public void vaticanReport(int popesBox,String player) {
        if (singlePlayer.getHasBeenCalled()[(popesBox/8)-1]) {
            throw new IllegalArgumentException("This Vatican Report has already been called by Lorenzo!");
        }
        if (singlePlayer.getBlackCrossToken() == popesBox) {
            broadcast(vv -> vv.sendPriorityMessage("Lorenzo is calling Vatican report #" + (popesBox/8)));
        } else {
            broadcast(vv -> vv.sendPriorityMessage("You are calling Vatican report #" + (popesBox/8)));
        }
        //controllo se il giocatore singolo ha raggiunto questa zona vaticana
        PopesFavorTrack singlePlayerPFT = singlePlayer.getPersonalDashboard().getPopesFavorTrack();
        singlePlayerPFT.hasReachedPopesZone(singlePlayerPFT.getTrackPosition());
        //aggiorno le tessere papali accumulate, se la zona vaticana è stata raggiunta
        if (singlePlayerPFT.getVaticanZone()[(popesBox/8)-1]) {
            int oldPoints = singlePlayerPFT.getPopesCardPoints();
            singlePlayerPFT.setPopesCardPoints(oldPoints + singlePlayerPFT.obtainPopesCardPoints(popesBox));
            broadcast(vv -> vv.sendPriorityMessage("Because you're in Vatican zone #" + (popesBox/8) + ", you are entitled to " + ((popesBox/8)+1) + " extra points!\n"));
        } else {
            broadcast(vv -> vv.sendPriorityMessage("You're not in Vatican zone #" + (popesBox/8) + ", hence you are not entitled to any points...\n"));
        }
        if (singlePlayer.getPersonalDashboard().getPopesFavorTrack().getTrackPosition() == popesBox || singlePlayer.getBlackCrossToken() == popesBox) {
            singlePlayer.vaticanReportHasBeenCalled(popesBox);
        }
    }

    public boolean getEndTurn() { return isEndTurn; }

    public void setEndTurn(boolean endTurn) {
        isEndTurn = endTurn;
    }

    /**
     * This method handles Lorenzo's turn when the player's one is over.
     * @throws IllegalAccessException if called during the player's turn instead of at its end.
     */
    public void lorenzoTurn() throws IllegalAccessException {
        if (isEndTurn) {
            // Esegui il segnalino Azione più in alto (indice 0)
            Action currentLorenzoAction = getSinglePlayer().getSoloActionTokens().get(0);
            try {
                currentLorenzoAction.execute();
            } catch (ActionException e) {
                broadcast(vv -> vv.sendPriorityMessage("\nLORENZO HAS WON, YOU HAVE LOST!\nThank you for playing. Have a nice day!"));
            }
            String actionType = currentLorenzoAction.getActionType().toString().toUpperCase();
            switch (actionType) {
                case "LORENZO":
                    broadcast(vv -> vv.lorenzoTurnActionNotification(actionType+"_"+((LorenzoAction) currentLorenzoAction).getBoxesForward(), "Lorenzo has gained " + ((LorenzoAction) currentLorenzoAction).getBoxesForward() + " positions in the Faith Track!"));
                    broadcast(vv -> vv.notification("Lorenzo's current position = " + getSinglePlayer().getBlackCrossToken(),false));
                    if (((LorenzoAction) currentLorenzoAction).getBoxesForward() == 1) {
                        broadcast(vv -> vv.notification("Since he gained one position, Solo Action Tokens will be shuffled.\n",false));
                    }
                    break;
                case "DEVCARDWITHDRAW":
                    broadcast(vv -> vv.lorenzoTurnActionNotification(actionType+"_"+((DiscardAction) currentLorenzoAction).getColorToDiscard(), "He has taken two " + ((DiscardAction) currentLorenzoAction).getColorToDiscard() + " cards from the cardset.\n"));
                    break;
                default:
                    throw new IllegalAccessException("a different kind of action is trying to be performed");
            }
            // Sposta in fondo alla pila il segnalino Azione scartato e segnala la fine del turno di Lorenzo
            getSinglePlayer().actionTokensShiftLeft();
            changeOfTurn();
        } else {
            throw new IllegalAccessException("It isn't Lorenzo's turn yet!");
        }
    }

    public void addThisPlayer(String username) {
        singlePlayer = new SinglePlayer(username, cards);
        addToPlayerList(singlePlayer);
        currentPlayer = singlePlayer;
        singlePlayer.getPersonalDashboard().addModelObserver(this);
        singlePlayer.getPersonalDashboard().getPopesFavorTrack().setTrackPosition(0);
    }

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
                    if (currentAction.getActionType() != Actions.DEPOSIT) {
                        Action finalCurrentAction = currentAction;
                        if (versionWithFunctioningSockets()){
                            broadcast(vv -> vv.actionMessage(finalCurrentAction.getPlayer().getUsername(), finalCurrentAction.getActionType()), currentAction.getPlayer().getUsername());
                        }
                    }
                    try {
                        currentAction.execute();
                        if (versionWithFunctioningSockets())
                            getVirtualView(currentAction.getPlayer()).acceptedAction();
                        if (!currentAction.multipleExecutions()) {
                            majorActionexecuted = true;
                        }
                    } catch (ActionException | NullPointerException e){
                        if (!e.getMessage().equals("end of the game")) {
                            getVirtualView(currentAction.getPlayer()).rejectedAction(e.getMessage());
                        } else {
                            // Se cattura una ActionException con quel messaggio, significa che la partita deve terminare subito.
                            // Si svuota la pila da tutte le altre azioni e si esegue solo quelle conclusive.
                            while (!actionQueue.isEmpty()) {
                                actionQueue.remove();
                            }
                            broadcast(vv -> vv.sendPriorityMessage("\nLORENZO HAS WON, YOU HAVE LOST!\nThank you for playing!"));
                            defeat = true;
                        }
                    }

                } else {
                    getVirtualView(currentAction.getPlayer()).actionTypeMessage("you cannot select this type of action for this turn. you can either activate a leader, or end the turn");
                }
            } catch (ActionException e) {
                getVirtualView(currentPlayer).rejectedAction(e.getMessage());
            }
        }
    }

    public void endGame() {
        while (!actionQueue.isEmpty()) {
            actionQueue.remove();
        }
        broadcast(vv -> vv.sendPriorityMessage("\nYOU HAVE WON, LORENZO HAS LOST!"));
        broadcast(vv -> vv.sendPriorityMessage("You have achieved " + singlePlayer.getAllVictoryPoints() + " points."));
        broadcast(vv -> vv.sendPriorityMessage("Thank you for playing!"));
        broadcast(VirtualView::invokeLogOut);
    }

    public boolean wasPlayerDefeated() { return defeat; }
}