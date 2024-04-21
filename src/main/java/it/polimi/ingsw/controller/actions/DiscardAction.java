package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.cards.Color;
import it.polimi.ingsw.model.cards.DevelopmentCard;
import it.polimi.ingsw.model.cards.LeaderCard;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.SinglePlayer;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.SpecialType;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * Strategy pattern: this is the action carried out when discarding either a Leader Card or a resource because of lack of space in the warehouse's shelves.
 */
public class DiscardAction extends Action {
    private List<Player> playerList;
    private CardSet cardSet = new CardSet();
    private boolean discardResource;
    private boolean discardBlackMarble = false;
    private String marbleToDiscard;
    private List<DevelopmentCard> developmentCardsToDiscard = new ArrayList<>();
    private Color colorToDiscard;
    private LeaderCard leadCard;
    private Actions actionType;
    private boolean singlePlayerGame = false;

    /**
     * Constructor when a player discards a Leader Card.
     * @param targetPlayer the player discarding the Leader Card
     */
    public DiscardAction(Player targetPlayer, LeaderCard leadCard) {
        super(targetPlayer);
        discardResource = false;
        this.leadCard = leadCard;
        actionType = Actions.DISCARDLEADER;
    }

    /**
     * Constructor when a player discard a Resource because of lack of space in the warehouse's shelves
     * @param playerList list of players whose faith points are incremented
     * @param targetPlayer player discarding the Resource, thus being excluded by the increment of faith points
     * @param blackMarble to determine whether a black marble is being discarded
     */
    public DiscardAction(List<Player> playerList, Player targetPlayer, String marbleToDiscard, boolean blackMarble) {
        super(targetPlayer);
        this.playerList = playerList;
        this.marbleToDiscard = marbleToDiscard;
        discardResource = true;
        discardBlackMarble = blackMarble;
        leadCard = null;
        actionType = Actions.DISCARDRESOURCE;
    }

    /**
     * Constructor of DiscardAction when Lorenzo takes two Development Cards from CardSet (single player game)
     * @param singlePlayer single player of the game
     * @param cardSet card set of the game
     */
    public DiscardAction(SinglePlayer singlePlayer, CardSet cardSet, Color dColor) {
        super(singlePlayer);
        this.cardSet = cardSet;
        colorToDiscard = dColor;
        leadCard = null;
        singlePlayerGame = true;
        actionType = Actions.DEVCARDWITHDRAW;
    }


    @Override
    public Actions getActionType() {
        return actionType;
    }

    /**
     * Executes the consequences of the action of discarding a Resource (discardResource = true) or a Leader Card (discardResource = false)
     * @throws ActionException if the player tries to discard a Leader Card which they don't possess
     */
    @Override
    public void execute() throws ActionException{
        int playerCounter = 0;
        // Caso partita multigiocatore
        if (!singlePlayerGame) {
            // Se il giocatore deve scartare una risorsa o una carta leader
            if (discardResource) {
                // Se il giocatore deve scartare una risorsa
                if (!discardBlackMarble) {
                    try {
                    targetPlayer.getPersonalDashboard().removeFromTray(new Marble(marbleToDiscard));
                    }catch (IllegalArgumentException e){
                        throw new ActionException(e.getMessage());
                    }
                } else {
                    try {
                        targetPlayer.getPersonalDashboard().removeFromTray(new Marble(SpecialType.BLACK));
                    }catch (IllegalArgumentException e){
                        throw new ActionException(e.getMessage());
                    }
                }
                for(Player player : playerList){
                    if (playerList.size() == 1) {
                        ((SinglePlayer) player).lorenzoMovesForward(1);
                    } else {
                        if (!player.equals(targetPlayer)) {
                            player.increaseTrackPosition(1);
                        }
                    }
                }
            } else {
                // Se il giocatore deve scartare una carta leader
                int leadCardsDeckSize = targetPlayer.getPersonalDashboard().getLeadCardsDeck().size();
                targetPlayer.getPersonalDashboard().getLeadCardsDeck().remove(leadCard);
                if(leadCardsDeckSize == targetPlayer.getPersonalDashboard().getLeadCardsDeck().size()){
                    throw new ActionException("leader card not present in the deck");
                }
                //notifica il giocatore del punto fede aquisito

                JSONObject jsonNotify = new JSONObject();
                jsonNotify.put("operation", "DISCARD LEADER CARD");
                jsonNotify.put("player", targetPlayer.getUsername());
                targetPlayer.getPersonalDashboard().notify(jsonNotify);

                //controlla se il giocatore non è nella fase in cui vengono assegnate le carte leader. in caso contrario aggiunge un punto fede
                if(targetPlayer.getPersonalDashboard().getLeadCardsDeck().size() < 2) {
                    targetPlayer.increaseTrackPosition(1);
                    targetPlayer.notifyActionFinished();
                }
            }
        } else {
            developmentCardsToDiscard = this.cardSet.getDevCardList();
            // Se si è in una partita singola e il giocatore scopre un segnalino Azione "-2 carte sviluppo"
            List<String> cardIds = new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                try {
                    DevelopmentCard cardToDiscard = cardSet.getDevCardList().stream()
                            .filter(i -> i.getColor() == colorToDiscard)
                            .min(Comparator.comparing(DevelopmentCard::getLevel))
                            .orElseThrow(NoSuchElementException::new);
                    cardSet.getCardListWithoutCard(cardToDiscard);
                    cardIds.add(cardToDiscard.getId());
                } catch (NoSuchElementException e) {
                    // Se quest'eccezione viene lanciata, allora non ci sono più carte di quel colore: Lorenzo vince e il giocatore perde!
                    // throw new ActionException("There are no other cards with color " + colorToDiscard + " to discard...");
                    throw new ActionException("end of the game");
                }
            }
            // Aggiornamento del cardset della view
            targetPlayer.getPersonalDashboard().notify(targetPlayer.getPersonalDashboard().notifyLorenzoTakingTwoCards(cardIds));
        }
    }

    @Override
    public boolean multipleExecutions() {
        return true;
    }

    public Color getColorToDiscard() {
        return colorToDiscard;
    }
}
