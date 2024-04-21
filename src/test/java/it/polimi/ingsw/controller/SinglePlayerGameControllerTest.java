package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.controller.actions.*;
import it.polimi.ingsw.controller.controllers.SinglePlayerGameController;
import it.polimi.ingsw.model.cards.Color;
import it.polimi.ingsw.model.cards.DevelopmentCard;
import it.polimi.ingsw.model.player.SinglePlayer;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.view.VirtualView;
import org.junit.Test;

import static org.junit.Assert.*;

public class SinglePlayerGameControllerTest {
    @Test
    public void vaticanReport() {
        SinglePlayerGameController spgc = new SinglePlayerGameController();
        spgc.addThisPlayer("pimar116");
        SinglePlayer sp = spgc.getSinglePlayer();
        sp.increaseTrackPosition(3);
        assertEquals(1, sp.getPopesFavorTrackPoints());

        sp.lorenzoMovesForward(8);
        assertEquals(1, sp.getPopesFavorTrackPoints());
        assertEquals(8, sp.getBlackCrossToken());


        sp.increaseTrackPosition(5);
        assertEquals(2, sp.getPopesFavorTrackPoints());

        sp.increaseTrackPosition(8);
        assertEquals(8, sp.getBlackCrossToken());
        assertEquals(12, sp.getPopesFavorTrackPoints());
    }

    @Test
    public void singlePlayerGameControllerTest() {
        SinglePlayerGameController spgc = new SinglePlayerGameController();
        assertNull(spgc.getSinglePlayer());
        spgc.addPlayer("pimar116", new VirtualView());
        try {
            spgc.addPlayer("pimar116", new VirtualView());
        } catch (IllegalStateException e) {
            assertEquals("player was already initialized", e.getMessage());
        }
        spgc.setPlayerOrder();
        assertEquals(7, ((SinglePlayer) spgc.getCurrentPlayer()).getSoloActionTokens().size());
        assertEquals(2, ((SinglePlayer) spgc.getCurrentPlayer()).getSoloActionTokens().stream().filter(a -> a.getActionType() == Actions.LORENZO && ((LorenzoAction) a).getBoxesForward() == 2).count());
        assertEquals(1, ((SinglePlayer) spgc.getCurrentPlayer()).getSoloActionTokens().stream().filter(a -> a.getActionType() == Actions.LORENZO && ((LorenzoAction) a).getBoxesForward() == 1).count());
        assertEquals(1, ((SinglePlayer) spgc.getCurrentPlayer()).getSoloActionTokens().stream().filter(a -> a.getActionType() == Actions.DEVCARDWITHDRAW && ((DiscardAction) a).getColorToDiscard() == Color.BLUE).count());
        assertEquals(1, ((SinglePlayer) spgc.getCurrentPlayer()).getSoloActionTokens().stream().filter(a -> a.getActionType() == Actions.DEVCARDWITHDRAW && ((DiscardAction) a).getColorToDiscard() == Color.GREEN).count());
        assertEquals(1, ((SinglePlayer) spgc.getCurrentPlayer()).getSoloActionTokens().stream().filter(a -> a.getActionType() == Actions.DEVCARDWITHDRAW && ((DiscardAction) a).getColorToDiscard() == Color.YELLOW).count());
        assertEquals(1, ((SinglePlayer) spgc.getCurrentPlayer()).getSoloActionTokens().stream().filter(a -> a.getActionType() == Actions.DEVCARDWITHDRAW && ((DiscardAction) a).getColorToDiscard() == Color.PURPLE).count());
        assertEquals("pimar116", spgc.getCurrentPlayer().getUsername());
        spgc.getCards().loadCards();
        assertEquals(48, spgc.getCards().getDevCardList().size());
        spgc.setEndTurn(false);
        spgc.getSinglePlayer().getPersonalDashboard().addToTray(new Marble(Resource.SERVANT));
        spgc.handleAction(new DepositAction(spgc.getSinglePlayer(), "RP", 2, 0, false));
        assertEquals(1, spgc.getSinglePlayer().getPersonalDashboard().getWarehouse().count(Resource.SERVANT));
        spgc.getSinglePlayer().getPersonalDashboard().getWarehouse().addToShelf(1, Resource.STONE);
        spgc.getSinglePlayer().getPersonalDashboard().getWarehouse().addToShelf(1, Resource.STONE);
        spgc.handleAction(new PurchaseAction(spgc.getSinglePlayer(), (DevelopmentCard) spgc.getCards().findCard("DC-1-YELLOW-RP-SR-1"), 2, spgc.getCards(), 0));
        assertEquals(47, spgc.getCards().getDevCardList().size());
        assertEquals(1, spgc.getSinglePlayer().getPersonalDashboard().getDevCardsDeck().size());
        int actualBlackCross = spgc.getSinglePlayer().getBlackCrossToken();
        int actualNumberOfCards = spgc.getCards().getDevCardList().size();
        int actualNumberOfGreenCards = (int) spgc.getCards().getDevCardList().stream().filter(dc -> dc.getColor() == Color.GREEN).count();
        spgc.handleAction(new LorenzoAction(spgc.getSinglePlayer(), 2));
        assertEquals(actualBlackCross+2, spgc.getSinglePlayer().getBlackCrossToken());
        actualBlackCross += 2;
        spgc.handleAction(new DiscardAction(spgc.getSinglePlayer(), spgc.getCards(), Color.GREEN));
        assertEquals(actualNumberOfCards-2, spgc.getCards().getDevCardList().size());
        actualNumberOfCards -= 2;
        actualNumberOfGreenCards = actualNumberOfCards - (int) spgc.getCards().getDevCardList().stream().filter(dc -> dc.getColor() != Color.GREEN).count();
        spgc.getSinglePlayer().increaseTrackPosition(6);
        assertEquals(6, spgc.getSinglePlayer().getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(2, spgc.getSinglePlayer().getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
        for (int i = 0; i < 8-actualBlackCross; i ++) {
            spgc.handleAction(new LorenzoAction(spgc.getSinglePlayer(), 1));
        }
        assertEquals(8, spgc.getSinglePlayer().getBlackCrossToken());
        assertEquals(2, spgc.getSinglePlayer().getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
        assertEquals(2, spgc.getSinglePlayer().getPersonalDashboard().getPopesFavorTrack().getPopesCardPoints());
        assertTrue(spgc.getSinglePlayer().getPersonalDashboard().getPopesFavorTrack().getVaticanZone()[0]);
        assertFalse(spgc.getSinglePlayer().getPersonalDashboard().getPopesFavorTrack().getVaticanZone()[1]);
        assertFalse(spgc.getSinglePlayer().getPersonalDashboard().getPopesFavorTrack().getVaticanZone()[2]);
        for (int i = 0; i < ((actualNumberOfGreenCards/2)+1); i++) {
            try {
                spgc.handleAction(new DiscardAction(spgc.getSinglePlayer(), spgc.getCards(), Color.GREEN));
            } catch (IllegalArgumentException e) {
                assertEquals("end of the game", e.getMessage());
            }
        }
        assertTrue(spgc.wasPlayerDefeated());
        assertEquals(0, (int) spgc.getCards().getDevCardList().stream().filter(dc -> dc.getColor() == Color.GREEN).count());

        spgc = new SinglePlayerGameController();
        assertNull(spgc.getSinglePlayer());
        spgc.addPlayer("pimar116", new VirtualView());
        try {
            spgc.addPlayer("pimar116", new VirtualView());
        } catch (IllegalStateException e) {
            assertEquals("player was already initialized", e.getMessage());
        }
        spgc.setPlayerOrder();
        try {
            spgc.lorenzoTurn();
        } catch (IllegalAccessException ignored) {}
        assertFalse(spgc.getEndTurn());
    }
}